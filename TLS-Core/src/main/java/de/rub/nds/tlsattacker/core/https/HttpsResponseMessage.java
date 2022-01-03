/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2022 Ruhr University Bochum, Paderborn University, Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.tlsattacker.core.https;

import de.rub.nds.modifiablevariable.HoldsModifiableVariable;
import de.rub.nds.modifiablevariable.ModifiableVariableFactory;
import de.rub.nds.modifiablevariable.string.ModifiableString;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.constants.ProtocolMessageType;
import de.rub.nds.tlsattacker.core.https.header.ContentLengthHeader;
import de.rub.nds.tlsattacker.core.https.header.DateHeader;
import de.rub.nds.tlsattacker.core.https.header.ExpiresHeader;
import de.rub.nds.tlsattacker.core.https.header.GenericHttpsHeader;
import de.rub.nds.tlsattacker.core.https.header.HostHeader;
import de.rub.nds.tlsattacker.core.https.header.HttpHeader;
import de.rub.nds.tlsattacker.core.https.header.LocationHeader;
import de.rub.nds.tlsattacker.core.https.header.TokenBindingHeader;
import de.rub.nds.tlsattacker.core.protocol.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.TlsContext;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class HttpsResponseMessage extends ProtocolMessage {

    private ModifiableString responseProtocol;

    private ModifiableString responseStatusCode;

    private ModifiableString responseContent;
    @XmlElementWrapper
    @XmlElements(value = { @XmlElement(type = GenericHttpsHeader.class, name = "HttpsHeader"),
        @XmlElement(type = ContentLengthHeader.class, name = "ContentLengthHeader"),
        @XmlElement(type = DateHeader.class, name = "DateHeader"),
        @XmlElement(type = ExpiresHeader.class, name = "ExpiresHeader"),
        @XmlElement(type = LocationHeader.class, name = "LocationHeader"),
        @XmlElement(type = HostHeader.class, name = "HostHeader"),
        @XmlElement(type = TokenBindingHeader.class, name = "TokenBindingHeader") })
    @HoldsModifiableVariable
    private List<HttpHeader> header;

    public HttpsResponseMessage() {
        protocolMessageType = ProtocolMessageType.APPLICATION_DATA;
        header = new LinkedList<>();
    }

    public HttpsResponseMessage(Config config) {
        protocolMessageType = ProtocolMessageType.APPLICATION_DATA;
        header = new LinkedList<>();
        header.add(new GenericHttpsHeader("Content-Type", "text/html; charset=UTF-8"));
        header.add(new LocationHeader());
        header.add(new ContentLengthHeader());
        header.add(new DateHeader());
        header.add(new ExpiresHeader());
        header.add(new GenericHttpsHeader("Cache-Control", "private, max-age=0"));
        header.add(new GenericHttpsHeader("Server", "GSE"));
    }

    public ModifiableString getResponseProtocol() {
        return responseProtocol;
    }

    public void setResponseProtocol(ModifiableString responseProtocol) {
        this.responseProtocol = responseProtocol;
    }

    public void setResponseProtocol(String responseProtocol) {
        this.responseProtocol = ModifiableVariableFactory.safelySetValue(this.responseProtocol, responseProtocol);
    }

    public ModifiableString getResponseStatusCode() {
        return responseStatusCode;
    }

    public void setResponseStatusCode(ModifiableString responseStatusCode) {
        this.responseStatusCode = responseStatusCode;
    }

    public void setResponseStatusCode(String responseStatusCode) {
        this.responseStatusCode = ModifiableVariableFactory.safelySetValue(this.responseStatusCode, responseStatusCode);
    }

    public ModifiableString getResponseContent() {
        return responseContent;
    }

    public void setResponseContent(ModifiableString responseContent) {
        this.responseContent = responseContent;
    }

    public void setResponseContent(String responseContent) {
        this.responseContent = ModifiableVariableFactory.safelySetValue(this.responseContent, responseContent);
    }

    public List<HttpHeader> getHeader() {
        return header;
    }

    public void setHeader(List<HttpHeader> header) {
        this.header = header;
    }

    @Override
    public String toCompactString() {
        return "HttpsResponseMessage";
    }

    @Override
    public HttpsResponseHandler getHandler(TlsContext context) {
        return new HttpsResponseHandler(context);
    }

    @Override
    public HttpsResponseParser getParser(TlsContext tlsContext, InputStream stream) {
        return new HttpsResponseParser(stream, tlsContext.getChooser().getSelectedProtocolVersion(),
            tlsContext.getConfig());
    }

    @Override
    public HttpsResponsePreparator getPreparator(TlsContext tlsContext) {
        return new HttpsResponsePreparator(tlsContext.getChooser(), this);
    }

    @Override
    public HttpsResponseSerializer getSerializer(TlsContext tlsContext) {
        return new HttpsResponseSerializer(this, tlsContext.getChooser().getSelectedProtocolVersion());
    }

    @Override
    public java.lang.String toShortString() {
        return "HTTPS_RES";
    }
}
