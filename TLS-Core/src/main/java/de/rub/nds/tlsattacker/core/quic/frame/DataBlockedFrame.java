/*
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2023 Ruhr University Bochum, Paderborn University, Technology Innovation Institute, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.tlsattacker.core.quic.frame;

import de.rub.nds.modifiablevariable.ModifiableVariableFactory;
import de.rub.nds.modifiablevariable.ModifiableVariableProperty;
import de.rub.nds.modifiablevariable.integer.ModifiableInteger;
import de.rub.nds.tlsattacker.core.quic.constants.QuicFrameType;
import de.rub.nds.tlsattacker.core.quic.handler.frame.QuicFrameHandler;
import de.rub.nds.tlsattacker.core.quic.parser.frame.QuicFrameParser;
import de.rub.nds.tlsattacker.core.quic.preparator.frame.QuicFramePreparator;
import de.rub.nds.tlsattacker.core.quic.serializer.frame.QuicFrameSerializer;
import de.rub.nds.tlsattacker.core.state.quic.QuicContext;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.InputStream;

@XmlRootElement
public class DataBlockedFrame extends QuicFrame {

    @ModifiableVariableProperty protected ModifiableInteger maximumData;

    private int maximumDataConfig;

    public DataBlockedFrame() {
        super(QuicFrameType.DATA_BLOCKED_FRAME);
    }

    public DataBlockedFrame(int maximumDataConfig) {
        this();
        this.maximumDataConfig = maximumDataConfig;
    }

    @Override
    public QuicFrameHandler getHandler(QuicContext context) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from
        // nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public QuicFrameSerializer getSerializer(QuicContext context) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from
        // nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public QuicFramePreparator getPreparator(QuicContext context) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from
        // nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public QuicFrameParser getParser(QuicContext context, InputStream stream) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from
        // nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public ModifiableInteger getMaximumData() {
        return maximumData;
    }

    public void setMaximumData(ModifiableInteger maximumData) {
        this.maximumData = maximumData;
    }

    public void setMaximumData(int maximumData) {
        this.maximumData = ModifiableVariableFactory.safelySetValue(this.maximumData, maximumData);
    }

    public int getMaximumDataConfig() {
        return maximumDataConfig;
    }

    public void setMaximumDataConfig(int maximumDataConfig) {
        this.maximumDataConfig = maximumDataConfig;
    }
}
