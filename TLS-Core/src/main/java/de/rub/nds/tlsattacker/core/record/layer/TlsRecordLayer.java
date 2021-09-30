/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2021 Ruhr University Bochum, Paderborn University, Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package de.rub.nds.tlsattacker.core.record.layer;

import de.rub.nds.tlsattacker.core.constants.ProtocolMessageType;
import de.rub.nds.tlsattacker.core.exceptions.ParserException;
import de.rub.nds.tlsattacker.core.exceptions.PreparationException;
import de.rub.nds.tlsattacker.core.protocol.parser.cert.CleanRecordByteSeperator;
import de.rub.nds.tlsattacker.core.record.AbstractRecord;
import de.rub.nds.tlsattacker.core.record.Record;
import de.rub.nds.tlsattacker.core.record.cipher.RecordCipher;
import de.rub.nds.tlsattacker.core.record.cipher.RecordNullCipher;
import de.rub.nds.tlsattacker.core.record.compressor.RecordCompressor;
import de.rub.nds.tlsattacker.core.record.compressor.RecordDecompressor;
import de.rub.nds.tlsattacker.core.record.crypto.Decryptor;
import de.rub.nds.tlsattacker.core.record.crypto.Encryptor;
import de.rub.nds.tlsattacker.core.record.crypto.RecordDecryptor;
import de.rub.nds.tlsattacker.core.record.crypto.RecordEncryptor;
import de.rub.nds.tlsattacker.core.record.parser.BlobRecordParser;
import de.rub.nds.tlsattacker.core.record.parser.RecordParser;
import de.rub.nds.tlsattacker.core.record.preparator.AbstractRecordPreparator;
import de.rub.nds.tlsattacker.core.record.serializer.AbstractRecordSerializer;
import de.rub.nds.tlsattacker.core.state.TlsContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TlsRecordLayer extends RecordLayer {

    private static final Logger LOGGER = LogManager.getLogger();

    protected final TlsContext tlsContext;

    private final Decryptor decryptor;
    private final Encryptor encryptor;

    private final RecordCompressor compressor;
    private final RecordDecompressor decompressor;

    public TlsRecordLayer(TlsContext tlsContext) {
        this.tlsContext = tlsContext;
        encryptor = new RecordEncryptor(new RecordNullCipher(tlsContext), tlsContext);
        decryptor = new RecordDecryptor(new RecordNullCipher(tlsContext), tlsContext);
        compressor = new RecordCompressor(tlsContext);
        decompressor = new RecordDecompressor(tlsContext);
    }

    @Override
    public void updateCompressor() {
        compressor.setMethod(tlsContext.getChooser().getSelectedCompressionMethod());
    }

    @Override
    public void updateDecompressor() {
        decompressor.setMethod(tlsContext.getChooser().getSelectedCompressionMethod());
    }

    @Override
    public List<AbstractRecord> parseRecords(byte[] rawRecordData) {
        List<AbstractRecord> records = new LinkedList<>();
        int dataPointer = 0;
        while (dataPointer != rawRecordData.length) {
            try {
                RecordParser parser =
                    new RecordParser(dataPointer, rawRecordData, tlsContext.getChooser().getSelectedProtocolVersion());
                Record record = parser.parse();
                records.add(record);
                if (dataPointer == parser.getPointer()) {
                    throw new ParserException("Ran into infinite Loop while parsing HttpsHeader");
                }
                dataPointer = parser.getPointer();
            } catch (ParserException e) {
                throw new ParserException("Could not parse provided Data as Record", e);
            }
        }
        LOGGER.debug("The protocol message(s) were collected from {} record(s). ", records.size());
        return records;
    }

    @Override
    public List<AbstractRecord> parseRecordsSoftly(byte[] rawRecordData) {
        List<AbstractRecord> records = new LinkedList<>();
        int dataPointer = 0;
        while (dataPointer != rawRecordData.length) {
            try {
                RecordParser parser =
                    new RecordParser(dataPointer, rawRecordData, tlsContext.getChooser().getSelectedProtocolVersion());
                Record record = parser.parse();
                records.add(record);
                if (dataPointer == parser.getPointer()) {
                    throw new ParserException("Ran into infinite Loop while parsing Records");
                }
                dataPointer = parser.getPointer();
            } catch (ParserException e) {
                LOGGER.debug("Could not parse Record, parsing as Blob");
                LOGGER.trace(e);
                BlobRecordParser blobParser = new BlobRecordParser(dataPointer, rawRecordData,
                    tlsContext.getChooser().getSelectedProtocolVersion());
                AbstractRecord record = blobParser.parse();
                records.add(record);
                if (dataPointer == blobParser.getPointer()) {
                    throw new ParserException("Ran into infinite Loop while parsing BlobRecords");
                }
                dataPointer = blobParser.getPointer();
            }
        }
        LOGGER.debug("The protocol message(s) were collected from {} record(s). ", records.size());
        return records;
    }

    @Override
    public byte[] prepareRecords(byte[] data, ProtocolMessageType contentType, List<AbstractRecord> records) {
        CleanRecordByteSeperator separator =
            new CleanRecordByteSeperator(records, tlsContext.getChooser().getOutboundMaxRecordDataSize(), 0, data);
        records = separator.parse();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        boolean useRecordType = false;
        if (contentType == null) {
            useRecordType = true;
        }
        for (AbstractRecord record : records) {
            if (useRecordType) {
                contentType = record.getContentMessageType();
                if (contentType == null) {
                    contentType = ProtocolMessageType.UNKNOWN;
                }
            }

            AbstractRecordPreparator preparator =
                record.getRecordPreparator(tlsContext.getChooser(), encryptor, compressor, contentType);
            preparator.prepare();
            AbstractRecordSerializer serializer = record.getRecordSerializer();
            try {
                byte[] recordBytes = serializer.serialize();
                record.setCompleteRecordBytes(recordBytes);
                stream.write(record.getCompleteRecordBytes().getValue());
            } catch (IOException ex) {
                throw new PreparationException("Could not write Record bytes to ByteArrayStream", ex);
            }
        }
        return stream.toByteArray();
    }

    @Override
    public void updateEncryptionCipher(RecordCipher encryptionCipher) {
        LOGGER.debug("Activating new EncryptionCipher (" + encryptionCipher.getClass().getSimpleName() + ")");
        encryptor.addNewRecordCipher(encryptionCipher);
    }

    @Override
    public void updateDecryptionCipher(RecordCipher decryptionCipher) {
        LOGGER.debug("Activating new DecryptionCipher (" + decryptionCipher.getClass().getSimpleName() + ")");
        decryptor.addNewRecordCipher(decryptionCipher);
    }

    @Override
    public void decryptAndDecompressRecord(AbstractRecord record) {
        if (record instanceof Record) {
            if (!getDecryptorCipher().getVersion().isTLS13() || (getDecryptorCipher().getVersion().isTLS13()
                && record.getContentMessageType() == ProtocolMessageType.APPLICATION_DATA)) {
                decryptor.decrypt(record);
                decompressor.decompress(record);
                ((Record) record).getComputations().setUsedTls13KeySetType(tlsContext.getActiveKeySetTypeRead());
            } else {
                // Do not decrypt the record
                record.prepareComputations();
                ((Record) record).setSequenceNumber(BigInteger.valueOf(tlsContext.getReadSequenceNumber()));
                byte[] protocolMessageBytes = record.getProtocolMessageBytes().getValue();
                record.setCleanProtocolMessageBytes(protocolMessageBytes);
                // tlsContext.increaseReadSequenceNumber();
            }
        } else {
            LOGGER.warn("Decrypting received non Record:" + record.toString());
            decryptor.decrypt(record);
            decompressor.decompress(record);
        }
    }

    @Override
    public AbstractRecord getFreshRecord() {
        return new Record(tlsContext.getChooser().getOutboundMaxRecordDataSize());
    }

    @Override
    public RecordCipher getEncryptorCipher() {
        return encryptor.getRecordMostRecentCipher();
    }

    @Override
    public RecordCipher getDecryptorCipher() {
        return decryptor.getRecordMostRecentCipher();
    }

    public Encryptor getEncryptor() {
        return encryptor;
    }

    public Decryptor getDecryptor() {
        return decryptor;
    }
}
