/*
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.tlsattacker.core.workflow.action;

import de.rub.nds.tlsattacker.core.exceptions.WorkflowExecutionException;
import de.rub.nds.tlsattacker.core.protocol.ProtocolMessage;
import de.rub.nds.tlsattacker.core.protocol.message.DtlsHandshakeMessageFragment;
import de.rub.nds.tlsattacker.core.record.AbstractRecord;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.state.TlsContext;
import de.rub.nds.tlsattacker.core.workflow.action.executor.ActionOption;
import de.rub.nds.tlsattacker.core.workflow.action.executor.MessageActionResult;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@XmlRootElement(name = "PopAndSend")
public class PopAndSendAction extends MessageAction implements SendingAction {

    private static final Logger LOGGER = LogManager.getLogger();

    /** Pop and send message with this index in message buffer. */
    private Integer index = null;

    private boolean couldPop = false;

    public PopAndSendAction() {
        super();
    }

    public PopAndSendAction(String connectionAlias) {
        super(connectionAlias);
    }

    public PopAndSendAction(String connectionAlias, int index) {
        super(connectionAlias);
        this.index = index;
    }

    @Override
    public void execute(State state) throws WorkflowExecutionException {
        TlsContext tlsContext = state.getTlsContext(connectionAlias);

        if (isExecuted()) {
            throw new WorkflowExecutionException("Action already executed!");
        }

        LinkedList<ProtocolMessage> messageBuffer = tlsContext.getMessageBuffer();
        if (index != null && index >= 0) {
            if (index >= messageBuffer.size()) {
                LOGGER.warn(
                        "Index out of bounds, "
                                + "trying to get element "
                                + index
                                + "of message buffer with "
                                + messageBuffer.size()
                                + "elements.");
            } else {
                messages.add(messageBuffer.get(index));
                messageBuffer.remove(index);
                tlsContext.getRecordBuffer().remove(index);
                couldPop = true;
            }
        } else {
            if (messageBuffer.isEmpty()) {
                LOGGER.warn("Message buffer is empty, nothing to send");
                return;
            } else {
                messages.add(messageBuffer.pop());
                couldPop = true;
            }
        }
        String sending = getReadableString(messages);
        if (connectionAlias == null) {
            LOGGER.info("Sending messages: " + sending);
        } else {
            LOGGER.info("Sending messages (" + connectionAlias + "): " + sending);
        }

        try {
            MessageActionResult result =
                    sendMessageHelper.sendMessages(messages, fragments, records, tlsContext, false);
            messages = new ArrayList<>(result.getMessageList());
            records = new ArrayList<>(result.getRecordList());
            if (result.getMessageFragmentList() != null) {
                fragments = new ArrayList<>(result.getMessageFragmentList());
            }
            setExecuted(true);
        } catch (IOException e) {
            LOGGER.debug(e);
            setExecuted(getActionOptions().contains(ActionOption.MAY_FAIL));
        }
    }

    @Override
    public String toString() {
        return "PopAndSendAction(index: " + index + ")";
    }

    @Override
    public boolean executedAsPlanned() {
        return isExecuted() && couldPop;
    }

    @Override
    public void setRecords(List<AbstractRecord> records) {
        this.records = records;
    }

    @Override
    public void setFragments(List<DtlsHandshakeMessageFragment> fragments) {
        this.fragments = fragments;
    }

    @Override
    public void reset() {
        messages = new LinkedList<>();
        records = new LinkedList<>();
        fragments = new LinkedList<>();
        setExecuted(null);
        couldPop = false;
    }

    @Override
    public List<ProtocolMessage> getSendMessages() {
        return messages;
    }

    @Override
    public List<AbstractRecord> getSendRecords() {
        return records;
    }

    @Override
    public List<DtlsHandshakeMessageFragment> getSendFragments() {
        return fragments;
    }

    @Override
    public MessageActionDirection getMessageDirection() {
        return MessageActionDirection.SENDING;
    }
}
