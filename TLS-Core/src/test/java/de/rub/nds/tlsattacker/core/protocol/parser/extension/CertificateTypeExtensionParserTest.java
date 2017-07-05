/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2017 Ruhr University Bochum / Hackmanit GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlsattacker.core.protocol.parser.extension;

import de.rub.nds.modifiablevariable.util.ArrayConverter;
import de.rub.nds.tlsattacker.core.constants.CertificateType;
import de.rub.nds.tlsattacker.core.constants.ExtensionType;
import de.rub.nds.tlsattacker.core.protocol.message.extension.CertificateTypeExtensionMessage;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 * @author Matthias Terlinde <matthias.terlinde@rub.de>
 */
@RunWith(Parameterized.class)
public class CertificateTypeExtensionParserTest {

    private final ExtensionType extensionType;
    private final byte[] expectedBytes;
    private final int extensionLength;
    private final int startParsing;
    private final int certificateTypesLength;
    private final List<CertificateType> certificateTypes;
    private final boolean isClientState;
    private CertificateTypeExtensionParser parser;
    private CertificateTypeExtensionMessage msg;

    public CertificateTypeExtensionParserTest(ExtensionType extensionType, byte[] expectedBytes, int extensionLength,
            int startParsing, int certificateTypesLength, List<CertificateType> certificateTypes, boolean isClientState) {
        this.extensionType = extensionType;
        this.expectedBytes = expectedBytes;
        this.extensionLength = extensionLength;
        this.startParsing = startParsing;
        this.certificateTypesLength = certificateTypesLength;
        this.certificateTypes = certificateTypes;
        this.isClientState = isClientState;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> generateData() {
        return Arrays.asList(new Object[][] {
                { ExtensionType.CERT_TYPE, ArrayConverter.hexStringToByteArray("0009000100"), 1, 0, 1,
                        Arrays.asList(CertificateType.X509), false },
                { ExtensionType.CERT_TYPE, ArrayConverter.hexStringToByteArray("000900020100"), 2, 0, 1,
                        Arrays.asList(CertificateType.X509), true },
                { ExtensionType.CERT_TYPE, ArrayConverter.hexStringToByteArray("00090003020100"), 3, 0, 2,
                        Arrays.asList(CertificateType.OPEN_PGP, CertificateType.X509), true } });
    }

    @Before
    public void setUp() {
        parser = new CertificateTypeExtensionParser(startParsing, expectedBytes);
    }

    @Test
    public void testParseExtensionMessageContent() {
        msg = parser.parse();

        assertArrayEquals(extensionType.getValue(), msg.getExtensionType().getValue());
        assertEquals(extensionLength, (int) msg.getExtensionLength().getValue());

        assertEquals(certificateTypesLength, (int) msg.getCertificateTypesLength().getValue());
        assertArrayEquals(CertificateType.toByteArray(certificateTypes), msg.getCertificateTypes().getValue());
    }

}
