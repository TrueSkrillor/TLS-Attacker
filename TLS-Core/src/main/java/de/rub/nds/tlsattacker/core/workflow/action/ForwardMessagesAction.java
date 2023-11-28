/*
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2023 Ruhr University Bochum, Paderborn University, Technology Innovation Institute, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.tlsattacker.core.workflow.action;

import de.rub.nds.modifiablevariable.HoldsModifiableVariable;
import de.rub.nds.tlsattacker.core.layer.LayerConfiguration;
import de.rub.nds.tlsattacker.core.layer.context.TlsContext;
import de.rub.nds.tlsattacker.core.protocol.ProtocolMessage;
import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@XmlRootElement(name = "ForwardMessages")
public class ForwardMessagesAction extends CommonForwardAction {

    private static final Logger LOGGER = LogManager.getLogger();

    @XmlElementWrapper @HoldsModifiableVariable @XmlElementRef
    protected List<ProtocolMessage> expectedMessages;

    public ForwardMessagesAction() {}

    public ForwardMessagesAction(
            String receiveFromAlias, String forwardToAlias, List<ProtocolMessage> messages) {
        this.expectedMessages = messages;
        this.receiveFromAlias = receiveFromAlias;
        this.forwardToAlias = forwardToAlias;
    }

    public ForwardMessagesAction(
            String receiveFromAlias, String forwardToAlias, ProtocolMessage... messages) {
        this(receiveFromAlias, forwardToAlias, new ArrayList<>(Arrays.asList(messages)));
    }

    public List<ProtocolMessage> getExpectedMessages() {
        return expectedMessages;
    }

    public void setExpectedMessages(List<ProtocolMessage> messages) {
        this.expectedMessages = messages;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("Forward Messages Action:\n");
        sb.append("Receive from alias: ").append(receiveFromAlias).append("\n");
        sb.append("\tExpected:");
        if ((expectedMessages != null)) {
            for (ProtocolMessage message : expectedMessages) {
                sb.append(", ");
                sb.append(message.toCompactString());
            }
        } else {
            sb.append(" (no messages set)");
        }
        sb.append("\n\tActual:");
        if ((getReceivedMessages() != null) && (!getReceivedMessages().isEmpty())) {
            for (ProtocolMessage message : getReceivedMessages()) {
                sb.append(message.toCompactString());
                sb.append(", ");
            }
        } else {
            sb.append(" (no messages set)");
        }
        sb.append("\n");
        sb.append("Forwarded to alias: ").append(forwardToAlias).append("\n");
        if (getSendMessages() != null) {
            sb.append("\t");
            for (ProtocolMessage message : getSendMessages()) {
                sb.append(message.toCompactString());
                sb.append(", ");
            }
            sb.append("\n");
        } else {
            sb.append("null (no messages set)");
        }
        return sb.toString();
    }

    @Override
    public Set<String> getAllSendingAliases() {
        return new HashSet<>(Collections.singleton(forwardToAlias));
    }

    @Override
    public Set<String> getAllReceivingAliases() {
        return new HashSet<>(Collections.singleton(receiveFromAlias));
    }

    @Override
    protected List<LayerConfiguration> createLayerConfiguration(TlsContext tlsContext) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createLayerConfiguration'");
    }
}
