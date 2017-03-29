/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2016 Ruhr University Bochum / Hackmanit GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlsattacker.tls.protocol.preparator;

import de.rub.nds.tlsattacker.tls.constants.HandshakeByteLength;
import de.rub.nds.tlsattacker.tls.constants.PRFAlgorithm;
import de.rub.nds.tlsattacker.tls.crypto.PseudoRandomFunction;
import de.rub.nds.tlsattacker.tls.crypto.TlsMessageDigest;
import de.rub.nds.tlsattacker.tls.protocol.message.FinishedMessage;
import de.rub.nds.tlsattacker.tls.workflow.TlsContext;
import de.rub.nds.tlsattacker.transport.ConnectionEnd;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Robert Merget - robert.merget@rub.de
 */
public class FinishedMessagePreparator extends HandshakeMessagePreparator<FinishedMessage> {

    private static final Logger LOGGER = LogManager.getLogger("PREPARATOR");

    private byte[] verifyData;
    private final FinishedMessage msg;

    public FinishedMessagePreparator(TlsContext context, FinishedMessage message) {
        super(context, message);
        this.msg = message;
    }

    @Override
    public void prepareHandshakeMessageContents() {
        verifyData = computeVerifyData();

        prepareVerifyData(msg);
    }

    private TlsMessageDigest getDigest() {
        TlsMessageDigest digest = context.getDigest();
        if (!digest.isInitialised()) {
            context.initiliazeTlsMessageDigest();
            digest = context.getDigest();
        }
        return digest;
    }

    private byte[] computeVerifyData() {
        PRFAlgorithm prfAlgorithm = context.getPRFAlgorithm();
        byte[] masterSecret = context.getMasterSecret();
        byte[] handshakeMessageHash = getDigest().digest();

        if (context.getConfig().getMyConnectionEnd() == ConnectionEnd.SERVER) {
            // TODO put this in seperate config option
            return PseudoRandomFunction.compute(prfAlgorithm, masterSecret, PseudoRandomFunction.SERVER_FINISHED_LABEL,
                    handshakeMessageHash, HandshakeByteLength.VERIFY_DATA);
        } else {
            return PseudoRandomFunction.compute(prfAlgorithm, masterSecret, PseudoRandomFunction.CLIENT_FINISHED_LABEL,
                    handshakeMessageHash, HandshakeByteLength.VERIFY_DATA);
        }
    }

    private void prepareVerifyData(FinishedMessage msg) {
        msg.setVerifyData(verifyData);
        LOGGER.debug("VerifyData: "+ Arrays.toString(msg.getVerifyData().getValue()));
    }

}
