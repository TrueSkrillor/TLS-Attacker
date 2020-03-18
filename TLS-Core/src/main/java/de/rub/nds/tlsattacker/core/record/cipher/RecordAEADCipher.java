/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2017 Ruhr University Bochum / Hackmanit GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlsattacker.core.record.cipher;

import de.rub.nds.modifiablevariable.util.ArrayConverter;
import de.rub.nds.tlsattacker.core.constants.AlgorithmResolver;
import de.rub.nds.tlsattacker.core.constants.ProtocolVersion;
import de.rub.nds.tlsattacker.core.crypto.cipher.CipherWrapper;
import de.rub.nds.tlsattacker.core.exceptions.CryptoException;
import de.rub.nds.tlsattacker.core.protocol.parser.Parser;
import de.rub.nds.tlsattacker.core.record.BlobRecord;
import de.rub.nds.tlsattacker.core.record.Record;
import de.rub.nds.tlsattacker.core.record.cipher.cryptohelper.KeySet;
import de.rub.nds.tlsattacker.core.state.TlsContext;
import de.rub.nds.tlsattacker.transport.ConnectionEndType;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RecordAEADCipher extends RecordCipher {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * AEAD tag length in bytes for regular ciphers
     */
    private static final int AEAD_TAG_LENGTH = 16;

    /**
     * AEAD tag length in bytes for CCM_8 ciphers
     */
    private static final int AEAD_CCM_8_TAG_LENGTH = 8;

    /**
     * Stores the computed tag length
     */
    private final int aeadTagLength;

    /**
     * Stores the computed tag length
     */
    private final int aeadExplicitLength;

    public RecordAEADCipher(TlsContext context, KeySet keySet) {
        super(context, keySet);
        ConnectionEndType localConEndType = context.getConnection().getLocalConnectionEndType();
        encryptCipher = CipherWrapper.getEncryptionCipher(cipherSuite, localConEndType, getKeySet());
        decryptCipher = CipherWrapper.getDecryptionCipher(cipherSuite, localConEndType, getKeySet());

        if (cipherSuite.isCCM_8()) {
            aeadTagLength = AEAD_CCM_8_TAG_LENGTH;
        } else {
            aeadTagLength = AEAD_TAG_LENGTH;
        }
        if (version.isTLS13()) {
            aeadExplicitLength = 0;
        } else {
            aeadExplicitLength = AlgorithmResolver.getCipher(cipherSuite).getNonceBytesFromRecord();
        }
    }

    public int getAeadSizeIncrease() {
        if (version.isTLS13()) {
            return aeadTagLength;
        } else {
            return aeadExplicitLength + aeadTagLength;
        }
    }

    private byte[] prepareEncryptionGcmNonce(byte[] aeadSalt, byte[] explicitNonce, Record record) {
        byte[] gcmNonce = ArrayConverter.concatenate(aeadSalt, explicitNonce);
        gcmNonce = encryptCipher.preprocessIv(record.getSequenceNumber().getValue().longValue(), gcmNonce);
        record.getComputations().setGcmNonce(gcmNonce);
        gcmNonce = record.getComputations().getGcmNonce().getValue();
        return gcmNonce;
    }

    private byte[] prepareEncryptionAeadSalt(Record record) {
        byte[] aeadSalt = getKeySet().getWriteIv(context.getConnection().getLocalConnectionEndType());
        record.getComputations().setAeadSalt(aeadSalt);
        aeadSalt = record.getComputations().getAeadSalt().getValue();
        return aeadSalt;
    }

    private byte[] prepareEncryptionExplicitNonce(Record record) {
        byte[] explicitNonce = createExplicitNonce();
        record.getComputations().setExplicitNonce(explicitNonce);
        explicitNonce = record.getComputations().getExplicitNonce().getValue();
        return explicitNonce;
    }

    private byte[] createExplicitNonce() {
        byte[] explicitNonce;
        if (aeadExplicitLength > 0) {
            explicitNonce = ArrayConverter.longToBytes(context.getWriteSequenceNumber(), aeadExplicitLength);
        } else {
            explicitNonce = new byte[aeadExplicitLength];
        }
        return explicitNonce;
    }

    @Override
    public void encrypt(Record record) throws CryptoException {
        LOGGER.debug("Encrypting Record");
        record.getComputations().setCipherKey(getKeySet().getWriteKey(context.getChooser().getConnectionEndType()));

        record.getComputations().setPlainRecordBytes(record.getCleanProtocolMessageBytes().getValue());

        byte[] explicitNonce = prepareEncryptionExplicitNonce(record);
        byte[] aeadSalt = prepareEncryptionAeadSalt(record);
        byte[] gcmNonce = prepareEncryptionGcmNonce(aeadSalt, explicitNonce, record);

        // TODO This does not make a lot of sense
        byte[] authenticatedNonMetaData = record.getComputations().getPlainRecordBytes().getValue();
        record.getComputations().setAuthenticatedNonMetaData(authenticatedNonMetaData);
        authenticatedNonMetaData = record.getComputations().getAuthenticatedNonMetaData().getValue();

        LOGGER.debug("Encrypting AEAD with the following IV: {}", ArrayConverter.bytesToHexString(gcmNonce));
        byte[] additionalAuthenticatedData = collectAdditionalAuthenticatedData(record, context.getChooser()
                .getSelectedProtocolVersion());
        record.getComputations().setAuthenticatedMetaData(additionalAuthenticatedData);
        additionalAuthenticatedData = record.getComputations().getAuthenticatedMetaData().getValue();

        LOGGER.debug("Encrypting AEAD with the following AAD: {}",
                ArrayConverter.bytesToHexString(additionalAuthenticatedData));

        byte[] plainBytes = record.getComputations().getPlainRecordBytes().getValue();
        byte[] wholeCipherText;
        if (version == ProtocolVersion.TLS12 || version == ProtocolVersion.TLS13
                || version == ProtocolVersion.TLS13_DRAFT25 || version == ProtocolVersion.TLS13_DRAFT26
                || version == ProtocolVersion.TLS13_DRAFT27 || version == ProtocolVersion.TLS13_DRAFT28) {
            wholeCipherText = encryptCipher.encrypt(gcmNonce, aeadTagLength * 8, additionalAuthenticatedData,
                    plainBytes);
        } else {
            wholeCipherText = encryptCipher.encrypt(gcmNonce, aeadTagLength * 8, plainBytes);
        }

        byte[] onlyCiphertext = Arrays.copyOfRange(wholeCipherText, 0, wholeCipherText.length - aeadTagLength);

        byte[] authenticationTag = Arrays.copyOfRange(wholeCipherText, wholeCipherText.length - aeadTagLength,
                wholeCipherText.length);
        record.getComputations().setAuthenticationTag(authenticationTag);
        authenticationTag = record.getComputations().getAuthenticationTag().getValue();

        record.getComputations().setCiphertext(onlyCiphertext);
        record.setProtocolMessageBytes(ArrayConverter.concatenate(explicitNonce, onlyCiphertext, authenticationTag));
        // TODO our own authentication tags are always valid
        record.getComputations().setAuthenticationTagValid(true);

    }

    @Override
    public void decrypt(Record record) throws CryptoException {
        LOGGER.debug("Decrypting Record");
        record.getComputations().setCipherKey(getKeySet().getReadKey(context.getChooser().getConnectionEndType()));

        byte[] protocolBytes = record.getProtocolMessageBytes().getValue();
        DecryptionParser parser = new DecryptionParser(0, protocolBytes);

        byte[] explicitNonce = parser.parseByteArrayField(aeadExplicitLength);
        record.getComputations().setExplicitNonce(explicitNonce);
        explicitNonce = record.getComputations().getExplicitNonce().getValue();

        byte[] salt = getKeySet().getReadIv(context.getConnection().getLocalConnectionEndType());
        record.getComputations().setAeadSalt(salt);
        salt = record.getComputations().getAeadSalt().getValue();

        byte[] cipherTextOnly = parser.parseByteArrayField(parser.getBytesLeft() - aeadTagLength);
        record.getComputations().setCiphertext(cipherTextOnly);
        record.getComputations().setAuthenticatedNonMetaData(record.getComputations().getCiphertext().getValue());

        byte[] additionalAuthenticatedData = collectAdditionalAuthenticatedData(record, context.getChooser()
                .getSelectedProtocolVersion());
        record.getComputations().setAuthenticatedMetaData(additionalAuthenticatedData);
        additionalAuthenticatedData = record.getComputations().getAuthenticatedMetaData().getValue();

        LOGGER.debug("Decrypting AEAD with the following AAD: {}",
                ArrayConverter.bytesToHexString(additionalAuthenticatedData));

        byte[] gcmNonce = ArrayConverter.concatenate(salt, explicitNonce);
        gcmNonce = decryptCipher.preprocessIv(record.getSequenceNumber().getValue().longValue(), gcmNonce);
        record.getComputations().setGcmNonce(gcmNonce);
        gcmNonce = record.getComputations().getGcmNonce().getValue();

        LOGGER.debug("Decrypting AEAD with the following IV: {}", ArrayConverter.bytesToHexString(gcmNonce));

        byte[] authenticationTag = parser.parseByteArrayField(parser.getBytesLeft());

        record.getComputations().setAuthenticationTag(authenticationTag);
        authenticationTag = record.getComputations().getAuthenticationTag().getValue();
        // TODO it would be better if we had a seperate CM implementation to do
        // the decryption

        try {
            byte[] plaintext;
            if (version == ProtocolVersion.TLS12 || version == ProtocolVersion.TLS13
                    || version == ProtocolVersion.TLS13_DRAFT25 || version == ProtocolVersion.TLS13_DRAFT26
                    || version == ProtocolVersion.TLS13_DRAFT27 || version == ProtocolVersion.TLS13_DRAFT28) {
                plaintext = decryptCipher.decrypt(gcmNonce, aeadTagLength * 8, additionalAuthenticatedData,
                        ArrayConverter.concatenate(cipherTextOnly, authenticationTag));
            } else {
                plaintext = decryptCipher.decrypt(gcmNonce, aeadTagLength * 8,
                        ArrayConverter.concatenate(cipherTextOnly, authenticationTag));
            }
            record.getComputations().setAuthenticationTagValid(true);
            record.getComputations().setPlainRecordBytes(plaintext);
            record.setCleanProtocolMessageBytes(record.getComputations().getPlainRecordBytes().getValue());
        } catch (CryptoException E) {
            LOGGER.warn("Tag invalid", E);
            record.getComputations().setAuthenticationTagValid(false);
            throw new CryptoException(E);
        }
    }

    @Override
    public void encrypt(BlobRecord br) throws CryptoException {
        LOGGER.debug("Encrypting BlobRecord");
        br.setProtocolMessageBytes(encryptCipher.encrypt(br.getCleanProtocolMessageBytes().getValue()));
    }

    @Override
    public void decrypt(BlobRecord br) throws CryptoException {
        LOGGER.debug("Derypting BlobRecord");
        br.setProtocolMessageBytes(decryptCipher.decrypt(br.getCleanProtocolMessageBytes().getValue()));

    }

    class DecryptionParser extends Parser<Object> {

        public DecryptionParser(int startposition, byte[] array) {
            super(startposition, array);
        }

        @Override
        public Object parse() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public byte[] parseByteArrayField(int length) {
            return super.parseByteArrayField(length);
        }

        @Override
        public int getBytesLeft() {
            return super.getBytesLeft();
        }

        @Override
        public int getPointer() {
            return super.getPointer();
        }

    }
}
