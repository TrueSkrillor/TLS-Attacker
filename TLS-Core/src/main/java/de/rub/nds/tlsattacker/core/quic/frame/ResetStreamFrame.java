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
public class ResetStreamFrame extends QuicFrame {

    @ModifiableVariableProperty protected ModifiableInteger streamId;

    @ModifiableVariableProperty protected ModifiableInteger applicationProtocolErrorCode;

    @ModifiableVariableProperty protected ModifiableInteger finalSize;

    private int streamIdConfig;
    private int applicationProtocolErrorCodeConfig;
    private int finalSizeConfig;

    public ResetStreamFrame() {
        super(QuicFrameType.RESET_STREAM_FRAME);
    }

    public ResetStreamFrame(
            int streamIdConfig, int applicationProtocolErrorCodeConfig, int finalSizeConfig) {
        this();
        this.streamIdConfig = streamIdConfig;
        this.applicationProtocolErrorCodeConfig = applicationProtocolErrorCodeConfig;
        this.finalSizeConfig = finalSizeConfig;
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

    public ModifiableInteger getStreamId() {
        return streamId;
    }

    public void setStreamId(ModifiableInteger streamId) {
        this.streamId = streamId;
    }

    public void setStreamId(int streamId) {
        this.streamId = ModifiableVariableFactory.safelySetValue(this.streamId, streamId);
    }

    public ModifiableInteger getApplicationProtocolErrorCode() {
        return applicationProtocolErrorCode;
    }

    public void setApplicationProtocolErrorCode(ModifiableInteger applicationProtocolErrorCode) {
        this.applicationProtocolErrorCode = applicationProtocolErrorCode;
    }

    public void setApplicationProtocolErrorCode(int applicationProtocolErrorCode) {
        this.applicationProtocolErrorCode =
                ModifiableVariableFactory.safelySetValue(
                        this.applicationProtocolErrorCode, applicationProtocolErrorCode);
    }

    public ModifiableInteger getFinalSize() {
        return finalSize;
    }

    public void setFinalSize(ModifiableInteger finalSize) {
        this.finalSize = finalSize;
    }

    public void setFinalSize(int finalSize) {
        this.finalSize = ModifiableVariableFactory.safelySetValue(this.finalSize, finalSize);
    }

    public int getStreamIdConfig() {
        return streamIdConfig;
    }

    public void setStreamIdConfig(int streamIdConfig) {
        this.streamIdConfig = streamIdConfig;
    }

    public int getApplicationProtocolErrorCodeConfig() {
        return applicationProtocolErrorCodeConfig;
    }

    public void setApplicationProtocolErrorCodeConfig(int applicationProtocolErrorCodeConfig) {
        this.applicationProtocolErrorCodeConfig = applicationProtocolErrorCodeConfig;
    }

    public int getFinalSizeConfig() {
        return finalSizeConfig;
    }

    public void setFinalSizeConfig(int finalSizeConfig) {
        this.finalSizeConfig = finalSizeConfig;
    }
}
