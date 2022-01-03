/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2022 Ruhr University Bochum, Paderborn University, Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.tlsattacker.core.protocol.message;

import de.rub.nds.modifiablevariable.HoldsModifiableVariable;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.protocol.ModifiableVariableHolder;
import de.rub.nds.tlsattacker.core.protocol.handler.DHClientKeyExchangeHandler;
import de.rub.nds.tlsattacker.core.protocol.message.computations.DHClientComputations;
import de.rub.nds.tlsattacker.core.protocol.parser.DHClientKeyExchangeParser;
import de.rub.nds.tlsattacker.core.protocol.preparator.DHClientKeyExchangePreparator;
import de.rub.nds.tlsattacker.core.protocol.serializer.DHClientKeyExchangeSerializer;
import de.rub.nds.tlsattacker.core.state.TlsContext;
import java.io.InputStream;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DHClientKeyExchangeMessage extends ClientKeyExchangeMessage {

    @HoldsModifiableVariable
    protected DHClientComputations computations;

    public DHClientKeyExchangeMessage() {
        super();
    }

    public DHClientKeyExchangeMessage(Config tlsConfig) {
        super(tlsConfig);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DHClientKeyExchangeMessage:");
        return sb.toString();
    }

    @Override
    public DHClientComputations getComputations() {
        return computations;
    }

    @Override
    public DHClientKeyExchangeHandler<? extends DHClientKeyExchangeMessage> getHandler(TlsContext context) {
        return new DHClientKeyExchangeHandler<>(context);
    }

    @Override
    public DHClientKeyExchangeParser getParser(TlsContext tlsContext, InputStream stream) {
        return new DHClientKeyExchangeParser(stream, tlsContext.getChooser().getLastRecordVersion(), tlsContext);
    }

    @Override
    public DHClientKeyExchangePreparator getPreparator(TlsContext tlsContext) {
        return new DHClientKeyExchangePreparator(tlsContext.getChooser(), this);
    }

    @Override
    public DHClientKeyExchangeSerializer getSerializer(TlsContext tlsContext) {
        return new DHClientKeyExchangeSerializer(this, tlsContext.getChooser().getSelectedProtocolVersion());
    }

    @Override
    public String toCompactString() {
        return "DH_CLIENT_KEY_EXCHANGE";
    }

    @Override
    public String toShortString() {
        return "DH_CKE";
    }

    @Override
    public void prepareComputations() {
        if (getComputations() == null) {
            computations = new DHClientComputations();
        }
    }

    @Override
    public List<ModifiableVariableHolder> getAllModifiableVariableHolders() {
        List<ModifiableVariableHolder> holders = super.getAllModifiableVariableHolders();
        if (computations != null) {
            holders.add(computations);
        }
        return holders;
    }
}
