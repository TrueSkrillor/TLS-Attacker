/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2022 Ruhr University Bochum, Paderborn University, Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package de.rub.nds.tlsattacker.core.http;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import de.rub.nds.modifiablevariable.HoldsModifiableVariable;
import de.rub.nds.modifiablevariable.ModifiableVariableFactory;
import de.rub.nds.modifiablevariable.string.ModifiableString;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.http.header.*;
import de.rub.nds.tlsattacker.core.layer.DataContainer;
import de.rub.nds.tlsattacker.core.layer.context.HttpContext;
import de.rub.nds.tlsattacker.core.protocol.*;

@XmlRootElement
public class HttpRequestMessage extends ModifiableVariableHolder
    implements DataContainer<HttpRequestMessage, HttpContext> {

    @XmlElementWrapper
    @XmlElements(value = { @XmlElement(type = GenericHttpHeader.class, name = "HttpHeader"),
        @XmlElement(type = ContentLengthHeader.class, name = "ContentLengthHeader"),
        @XmlElement(type = DateHeader.class, name = "DateHeader"),
        @XmlElement(type = ExpiresHeader.class, name = "ExpiresHeader"),
        @XmlElement(type = LocationHeader.class, name = "LocationHeader"),
        @XmlElement(type = HostHeader.class, name = "HostHeader"),
        @XmlElement(type = TokenBindingHeader.class, name = "TokenBindingHeader"),
        @XmlElement(type = TokenBindingHeader.class, name = "CookieHeader") })
    @HoldsModifiableVariable
    private List<HttpHeader> header;

    private ModifiableString requestType;

    private ModifiableString requestPath;

    private ModifiableString requestProtocol;

    public HttpRequestMessage() {
        super();
        header = new LinkedList<>();
    }

    public HttpRequestMessage(Config config) {
        super();
        header = new LinkedList<>();
        header.add(new HostHeader());
        header.add(new GenericHttpHeader("Connection", "keep-alive"));
        header.add(new GenericHttpHeader("Accept",
            "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8"));
        header.add(new GenericHttpHeader("Accept-Encoding", "identity"));
        header.add(new GenericHttpHeader("Accept-Language", "de-DE,de;q=0.8,en-US;q=0.6,en;q=0.4"));
        if (config.isAddTokenBindingExtension()) {
            header.add(new TokenBindingHeader());
        }
        if (config.isAddHttpCookie()) {
            header.add(new CookieHeader());
        }
        header.add(new GenericHttpHeader("Upgrade-Insecure-Requests", "1"));
        header.add(new GenericHttpHeader("User-Agent",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/59.0.3071.109 Chrome/59.0.3071.109 Safari/537.36"));
    }

    public List<HttpHeader> getHeader() {
        return header;
    }

    public void setHeader(List<HttpHeader> header) {
        this.header = header;
    }

    public ModifiableString getRequestType() {
        return requestType;
    }

    public void setRequestType(ModifiableString requestType) {
        this.requestType = requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = ModifiableVariableFactory.safelySetValue(this.requestType, requestType);
    }

    public ModifiableString getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(ModifiableString requestPath) {
        this.requestPath = requestPath;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = ModifiableVariableFactory.safelySetValue(this.requestPath, requestPath);
    }

    public ModifiableString getRequestProtocol() {
        return requestProtocol;
    }

    public void setRequestProtocol(ModifiableString requestProtocol) {
        this.requestProtocol = requestProtocol;
    }

    public void setRequestProtocol(String requestProtocol) {
        this.requestProtocol = ModifiableVariableFactory.safelySetValue(this.requestProtocol, requestProtocol);
    }

    public String toCompactString() {
        return "HttpRequestMessage";
    }

    public String toShortString() {
        return "HTTP_REQ";
    }

    public HttpRequestHandler getHandler(HttpContext context) {
        return new HttpRequestHandler(context);
    }

    public HttpRequestParser getParser(HttpContext context, InputStream stream) {
        return new HttpRequestParser(stream);
    }

    public HttpRequestPreparator getPreparator(HttpContext context) {
        return new HttpRequestPreparator(context, this);
    }

    public HttpRequestSerializer getSerializer(HttpContext context) {
        return new HttpRequestSerializer(this);
    }
}
