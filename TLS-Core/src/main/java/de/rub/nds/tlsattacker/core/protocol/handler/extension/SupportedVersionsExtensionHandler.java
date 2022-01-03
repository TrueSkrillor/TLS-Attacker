/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2022 Ruhr University Bochum, Paderborn University, Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.tlsattacker.core.protocol.handler.extension;

import de.rub.nds.tlsattacker.core.constants.HandshakeByteLength;
import de.rub.nds.tlsattacker.core.constants.ProtocolVersion;
import de.rub.nds.tlsattacker.core.exceptions.AdjustmentException;
import de.rub.nds.tlsattacker.core.protocol.message.extension.SupportedVersionsExtensionMessage;
import de.rub.nds.tlsattacker.core.state.TlsContext;
import de.rub.nds.tlsattacker.transport.ConnectionEndType;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This handler processes the SupportedVersions extensions, as defined in
 * https://tools.ietf.org/html/draft-ietf-tls-tls13-21#section-4.2.1
 */
public class SupportedVersionsExtensionHandler extends ExtensionHandler<SupportedVersionsExtensionMessage> {

    private static final Logger LOGGER = LogManager.getLogger();

    public SupportedVersionsExtensionHandler(TlsContext context) {
        super(context);
    }

    @Override
    public void adjustTLSExtensionContext(SupportedVersionsExtensionMessage message) {
        byte[] versionBytes = message.getSupportedVersions().getValue();
        if (versionBytes.length % HandshakeByteLength.VERSION != 0) {
            throw new AdjustmentException("Could not create reasonable ProtocolVersions from VersionBytes");
        }
        List<ProtocolVersion> versionList = ProtocolVersion.getProtocolVersions(versionBytes);
        if (context.getTalkingConnectionEndType() == ConnectionEndType.CLIENT) {
            context.setClientSupportedProtocolVersions(versionList);
            context.setHighestClientProtocolVersion(ProtocolVersion.getHighestProtocolVersion(versionList));
        } else {
            if (context.getConfig().isEnforceSettings()) {
                context.setSelectedProtocolVersion(context.getChooser().getHighestProtocolVersion());
                return;
            }
            if (versionList.size() == 1) {
                context.setSelectedProtocolVersion(versionList.get(0));
            } else {
                LOGGER.warn("Received a SupportedProtocolVersionExtension with unknown contents");
            }
        }
    }
}
