/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2021 Ruhr University Bochum, Paderborn University, Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package de.rub.nds.tlsattacker.core.protocol.handler.extension;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.exceptions.CryptoException;
import de.rub.nds.tlsattacker.core.protocol.message.extension.SessionTicketTLSExtensionMessage;
import de.rub.nds.tlsattacker.core.protocol.parser.extension.SessionTicketTLSExtensionParser;
import de.rub.nds.tlsattacker.core.protocol.preparator.extension.SessionTicketTLSExtensionPreparator;
import de.rub.nds.tlsattacker.core.protocol.serializer.extension.SessionTicketTLSExtensionSerializer;
import de.rub.nds.tlsattacker.core.state.Session;
import de.rub.nds.tlsattacker.core.state.StatePlaintext;
import de.rub.nds.tlsattacker.core.state.TlsContext;
import de.rub.nds.tlsattacker.core.state.parser.StatePlaintextParser;
import de.rub.nds.tlsattacker.core.util.StaticTicketCrypto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SessionTicketTlsExtensionHandler extends ExtensionHandler<SessionTicketTLSExtensionMessage> {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Constructor
     *
     * @param context
     *                The TlsContext which the Handler should adjust
     */
    public SessionTicketTlsExtensionHandler(TlsContext context) {
        super(context);
    }

    @Override
    public SessionTicketTLSExtensionParser getParser(byte[] message, int pointer, Config config) {
        return new SessionTicketTLSExtensionParser(pointer, message, config);
    }

    @Override
    public SessionTicketTLSExtensionPreparator getPreparator(SessionTicketTLSExtensionMessage message) {
        return new SessionTicketTLSExtensionPreparator(context.getChooser(), message, getSerializer(message));
    }

    @Override
    public SessionTicketTLSExtensionSerializer getSerializer(SessionTicketTLSExtensionMessage message) {
        return new SessionTicketTLSExtensionSerializer(message);
    }

    @Override
    public void adjustTLSExtensionContext(SessionTicketTLSExtensionMessage message) {
        if (message.getExtensionLength().getValue() > 65535) {
            LOGGER.warn("The SessionTLS ticket length shouldn't exceed 2 bytes as defined in RFC 4507. " + "Length was "
                + message.getExtensionLength().getValue());
        }

        if (message.getExtensionLength().getValue() > 0) {
            LOGGER.debug("Adjusting for client offered session ticket");
            if (context.getTalkingConnectionEndType() != context.getChooser().getConnectionEndType()) {
                StatePlaintext statePlaintext = getStateFromTicket(message);
                if (statePlaintext != null) {
                    LOGGER.info("Resuming Session using Ticket");
                    context.setMasterSecret(statePlaintext.getMasterSecret().getValue());
                    // TODO: rework interaction with SessionID
                }
            } else {
                // TODO: rework interaction with SessionID, use ticket value
                LOGGER.debug("Adding Session for Ticket resumption using dummy SessionID");
                Session session = new Session(context.getConfig().getDefaultClientTicketResumptionSessionId(),
                    context.getChooser().getMasterSecret());
                context.addNewSession(session);
            }
        }
        context.setSessionTicketTLS(message.getSessionTicket().getIdentity().getValue());
    }

    private StatePlaintext getStateFromTicket(SessionTicketTLSExtensionMessage message) {
        try {
            byte[] decryptedState = decryptState(message.getSessionTicket().getEncryptedState().getValue(),
                message.getSessionTicket().getIV().getValue());
            StatePlaintextParser stateParser = new StatePlaintextParser(0, decryptedState);
            return stateParser.parse();
        } catch (CryptoException ex) {
            LOGGER.warn("Was unable to decrypt session ticket ", ex);
            return null;
        }
    }

    private byte[] decryptState(byte[] encryptedState, byte[] iv) throws CryptoException {
        Config config = context.getConfig();
        return StaticTicketCrypto.decrypt(config.getSessionTicketCipherAlgorithm(), encryptedState,
            config.getSessionTicketEncryptionKey(), iv);
    }
}
