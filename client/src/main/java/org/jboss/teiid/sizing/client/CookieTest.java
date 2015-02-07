package org.jboss.teiid.sizing.client;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.io.PrintWriter;
import java.security.cert.CertificateException;
import java.util.Map;

public class CookieTest {

    static String URL = "https://idp.dev2.redhat.com/idp/authUser";
    static final String USER = "rhn-support-jshi";
    static final String PASS = "redhat";

    public static void main(String[] args) throws Exception {

        // Create a trust manager that does not validate certificate chains
        final TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] x509Certificates, String s) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] x509Certificates, String s) throws CertificateException {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[0];
                    }
                }
        };

        // Install the all-trusting trust manager
        final SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

        // step 1
        Client client = ClientBuilder.newBuilder()
                .sslContext(sslContext)
                .build();

        WebTarget target = client.target("https://idp.dev2.redhat.com/idp/authUser")
                .queryParam("j_username", "rhn-support-jshi")
                .queryParam("j_password", "redhat");

        Response resp = target.request().get();

        Map<String, NewCookie> cookies = resp.getCookies();

        final String JSESSIONID = "JSESSIONID";
        final String JSESSIONIDSSO = "JSESSIONIDSSO";
        final String IDP_PUBLIC = "devsmwdev2-sticky-idp-public";

        String jSessionIdVal = cookies.get(JSESSIONID).getValue();
        String jSessionIdSsoVal = cookies.get(JSESSIONIDSSO).getValue();
        String idpPublic = cookies.get(IDP_PUBLIC).getValue();
        System.out.println(JSESSIONID + ": " + jSessionIdVal);
        System.out.println(JSESSIONIDSSO + ": " + jSessionIdSsoVal);
        System.out.println(IDP_PUBLIC + ": " + idpPublic);
        dumpCookies(resp);
        client.close();

        // step 2

        Client client2 = ClientBuilder.newBuilder()
                .sslContext(sslContext)
                .build();
        WebTarget target2 = client2.target("https://ams-dev2.devlab.redhat.com/wapps/sso/login.html?redirect=https%3A%2F%2Fidp.dev2.redhat.com%3A443%2Fidp%2F");
        Response resp2 = target2.request().post(null);
        resp2.bufferEntity();
        dumpCookies(resp2);

        final String amsJSessionIdVal = resp2.getCookies().get(JSESSIONID).getValue();
        System.out.println("amsJSessionIdVal: " + amsJSessionIdVal);


        client2.close();

        // step 3

/*
<saml2p:AuthnRequest xmlns:saml2p="urn:oasis:names:tc:SAML:2.0:protocol"
                     AssertionConsumerServiceURL="https://ams-dev2.devlab.redhat.com/wapps/sso/consume.html"
                     Destination="https://idp.dev2.redhat.com/idp/" ID="_af50c689-dadd-4bb5-afc8-06ac1cc03208"
                     IssueInstant="2015-02-06T15:57:00.395Z" Version="2.0">
    <saml2:Issuer xmlns:saml2="urn:oasis:names:tc:SAML:2.0:assertion">https://ams-dev2.devlab.redhat.com/wapps/sso/
    </saml2:Issuer>
    <saml2p:NameIDPolicy AllowCreate="true" Format="urn:oasis:names:tc:SAML:2.0:nameid-format:transient"/>
</saml2p:AuthnRequest>
 */
        final String SAMLRequest = "PHNhbWwycDpBdXRoblJlcXVlc3QgeG1sbnM6c2FtbDJwPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6cHJvdG9jb2wiIEFzc2VydGlvbkNvbnN1bWVyU2VydmljZVVSTD0iaHR0cHM6Ly9hbXMtZGV2Mi5kZXZsYWIucmVkaGF0LmNvbS93YXBwcy9zc28vY29uc3VtZS5odG1sIiBEZXN0aW5hdGlvbj0iaHR0cHM6Ly9pZHAuZGV2Mi5yZWRoYXQuY29tL2lkcC8iIElEPSJfYWY1MGM2ODktZGFkZC00YmI1LWFmYzgtMDZhYzFjYzAzMjA4IiBJc3N1ZUluc3RhbnQ9IjIwMTUtMDItMDZUMTU6NTc6MDAuMzk1WiIgVmVyc2lvbj0iMi4wIj48c2FtbDI6SXNzdWVyIHhtbG5zOnNhbWwyPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXNzZXJ0aW9uIj5odHRwczovL2Ftcy1kZXYyLmRldmxhYi5yZWRoYXQuY29tL3dhcHBzL3Nzby88L3NhbWwyOklzc3Vlcj48c2FtbDJwOk5hbWVJRFBvbGljeSBBbGxvd0NyZWF0ZT0idHJ1ZSIgRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6bmFtZWlkLWZvcm1hdDp0cmFuc2llbnQiLz48L3NhbWwycDpBdXRoblJlcXVlc3Q+";
        final String RelayState = "https://idp.dev2.redhat.com:443/idp/";

        Client client3 = ClientBuilder.newBuilder()
                .sslContext(sslContext)
                .build();

        WebTarget target3 = client3.target("https://idp.dev2.redhat.com/idp/");

        Form samlRequestForm = new Form().param("SAMLRequest", SAMLRequest).param("RelayState", RelayState);

        Response resp3 = target3.request().
                cookie(JSESSIONID, jSessionIdSsoVal).
                cookie(JSESSIONIDSSO, jSessionIdSsoVal).
                cookie(IDP_PUBLIC, idpPublic).
                post(Entity.form(samlRequestForm));

        resp3.bufferEntity();

        String html = resp3.readEntity(String.class);

        Document doc = Jsoup.parse(html);
        String samlResponse = null;
        for (Element element : doc.getAllElements()) {
            if (element.getClass() == (FormElement.class)) {
                FormElement form = (FormElement) element;
                samlResponse = form.getElementsByAttributeValue("name", "SAMLResponse").get(0).val();
            }
        }

        /*
        <samlp:Response xmlns:samlp="urn:oasis:names:tc:SAML:2.0:protocol" xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion"
                Destination="https://ams-dev2.devlab.redhat.com/wapps/sso/consume.html"
                ID="ID_0203a118-ebfb-4663-a578-89d487b25a92" InResponseTo="_af50c689-dadd-4bb5-afc8-06ac1cc03208"
                IssueInstant="2015-02-07T03:35:09.053Z" Version="2.0">
<saml:Issuer xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion">https://idp.dev2.redhat.com/idp/</saml:Issuer>
<dsig:Signature xmlns:dsig="http://www.w3.org/2000/09/xmldsig#">
    <dsig:SignedInfo>
        <dsig:CanonicalizationMethod Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#"/>
        <dsig:SignatureMethod Algorithm="http://www.w3.org/2000/09/xmldsig#rsa-sha1"/>
        <dsig:Reference URI="#ID_0203a118-ebfb-4663-a578-89d487b25a92">
            <dsig:Transforms>
                <dsig:Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature"/>
                <dsig:Transform Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#"/>
            </dsig:Transforms>
            <dsig:DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1"/>
            <dsig:DigestValue>/FR4XVH1Z+XTx5nLLCGnR5ees8c=</dsig:DigestValue>
        </dsig:Reference>
    </dsig:SignedInfo>
    <dsig:SignatureValue>
        aQdrF7c6/VeMavHhFk662xSR83v3KcA6ZWjwm5W2IT+YYNcl5FlgeMElxGaw8d6Io6kec7OMksm7wCkWMYEaXDleYN1KrRl/nSk2MZy+4Ko6/4aFqztPcHaT77oVsxydR54hZiW960g9ieifr2dWJGgwM+CDN9BgFAmQW1dEMIvyOSFbwZnV1bAoiz17/PngqSXD9ZlV2ShI+bZDvRRkivvjAxPuN02yxuLS8R4ejAySp9L+6lTBMiY2FwJi39Z3AW5sH/eplHlz3Fd5HJtaJZjX0rXUhWIT/WzMiFbO8QtJmY8uQBwHZbP9mkGfVMI8Gq4xvT0kqTff627an3NxVd0/Hk7jiGWjDZsv731/B3EQghbqWjizHosuyqFumB7TD1j00umY6x+gULYlDqbv1qKA0gXeGEsX1EUi6PhQEtNZe4e4nm0d8eXb8NrNjyy4aIWw7pH6ockO5iZwIza0+w2uL5dYigZZhwRKj+bSzgt3FZP5k/IaFW7xtXk+DMtx8/2dELiQy7FusZ+MEwfdtRHwSjozcAo7pxa1XmYmNhjwGDdTPGVIEMTuWZ3QS+Ms26V0EIDKOy4gndOBwpGr6+ZSugXXNayCZFncfJJya/9OYmDNrnrGzhgUGtsQrxgrjXvjJie8xn7D7bCE9ZoquCgukW5MGRhc+42/Izmi+Nk=
    </dsig:SignatureValue>
    <dsig:KeyInfo>
        <dsig:KeyValue>
            <dsig:RSAKeyValue>
                <dsig:Modulus>
                    ukhdFuyogggwhsDlnLNRdR1vI1XRwaU5PLC+NFePPhckLCLWQO6R1JACYzv5d2+gxGo5kMLTgZ87O6LOnu8bLlPXwz/Ad4NAskcBGwRVI09/OOSKbeITfF6XkBkft0pXMA1Dee0HsSPw9PrcOHuXa+fSr9QMMi1+KYYuQ19aQ6jJ139KhPbCb89WEdfWQSFo5mjmaxsyF3lySLR3366HrgadKBESdEiuByCzhTd2KQNSlPNBbFKOT8B4scD1B0+/TqilT1dI3eFso+XcE9CDjJZBiDaVGMXDO22ZENeIj3HhdEuSgeA7oa0huOp7UItHMvGmWzmmx9MQkpBIvfprZntxSBCxHlOVl6mT1Q7C/wsGNBnIqUGNzA06nGcN4UL6SJi0qR6vVWv2mJjkUvEaehd1Vqac5MoDBw86Ta6wZhrFNaaj7UQBDWVf1QLFt64eNI/PNIYf8BwZ9kQFzK97LFv/7t75B8PzpW4dzOt2jTl6iR0p9d37oP3pGRyUOfJqJw1vvmbhvxh3PnlaCWAgobd0XC3/DuOgTDRlO/VQRHGa6pjbyT2AQzELrf0YS85WtGqRlW4oQKMl/b2LPRFNOkDw4DDN6cLwQ9J02b//6GOnlXVW2YlsTt+JZY3qKHY4//xby61GxIdhfPBn/v3fRupSR6V5gfSoDBmO/3nlxzk=
                </dsig:Modulus>
                <dsig:Exponent>AQAB</dsig:Exponent>
            </dsig:RSAKeyValue>
        </dsig:KeyValue>
    </dsig:KeyInfo>
</dsig:Signature>
<samlp:Status>
    <samlp:StatusCode Value="urn:oasis:names:tc:SAML:2.0:status:Success"/>
</samlp:Status>
<saml:Assertion xmlns="urn:oasis:names:tc:SAML:2.0:assertion" ID="ID_7fdff6dc-eb03-44cb-8c56-bcd85c909a2d"
                IssueInstant="2015-02-07T03:35:09.052Z" Version="2.0"
                xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion">
    <saml:Issuer xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion">https://idp.dev2.redhat.com/idp/</saml:Issuer>
    <dsig:Signature xmlns:dsig="http://www.w3.org/2000/09/xmldsig#">
        <dsig:SignedInfo>
            <dsig:CanonicalizationMethod Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#"/>
            <dsig:SignatureMethod Algorithm="http://www.w3.org/2000/09/xmldsig#rsa-sha1"/>
            <dsig:Reference URI="#ID_7fdff6dc-eb03-44cb-8c56-bcd85c909a2d">
                <dsig:Transforms>
                    <dsig:Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature"/>
                    <dsig:Transform Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#"/>
                </dsig:Transforms>
                <dsig:DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1"/>
                <dsig:DigestValue>RYcDpYFSPGV34rYTdBEHETqCGqk=</dsig:DigestValue>
            </dsig:Reference>
        </dsig:SignedInfo>
        <dsig:SignatureValue>
            glkmFkh7hKe/ZiNSJqZHrbGT3+hT/Stxglrv4UvSI5/ubF786uWP0F3Vnp+v9sWeGGiQ2mgqBs8QRMTyt4tzuztB2HpHFyIubsNaofQZMsBJ2Bnn5fQx+dRBGmS/ol2m/AX4w3kzKxzZItpFnIgw7t+6u3jwkIAGDk/6Q7RCaS6g75FS5fPaTL9dl0YKAPh02lhPqxvc+nb9l+vlvmSKMnD7r4cU0HPuEfigxFLhVqH3qPDbAOapPHisdIxHPSc27d+rC/zyq8n1PulhY9/SuCBfr6vq0iNo2DZ18C5OZN9vg74Fopmsom84BmDm9jbvBEQxNFAq1gXhTORznFcOX6wsnbQBsH41Eb/hP3gOGUXpKuakswLPLL9HTAX9pw1kFfAF8lnLmqJ1akbajvRpn6PQY8qQcmz7aFEfo09bFkry7thrvhPnN7jQVvgnwFDOWnFDmVWJfwoGNGOV0i4mBg82GT31o3mhN1zkVx+uRq4zfKEisEW5pTROsQ31+7kpeE0fWWvBLNd/3otM0ewa3Bu8OtgS/HLoIFIAdFLNDAf92UaYf5plm6JQisEnxMpb2q4oqAqFEceHgLY8KESeA8PRgrWnbRKMS7cSnJuQumYaYGYHMm67puL6lmFpmXQpbj+1N9bOlDCLgb9jWfyeSPmf9fcYZtSOvYVDl39XJ5U=
        </dsig:SignatureValue>
        <dsig:KeyInfo>
            <dsig:KeyValue>
                <dsig:RSAKeyValue>
                    <dsig:Modulus>
                        ukhdFuyogggwhsDlnLNRdR1vI1XRwaU5PLC+NFePPhckLCLWQO6R1JACYzv5d2+gxGo5kMLTgZ87O6LOnu8bLlPXwz/Ad4NAskcBGwRVI09/OOSKbeITfF6XkBkft0pXMA1Dee0HsSPw9PrcOHuXa+fSr9QMMi1+KYYuQ19aQ6jJ139KhPbCb89WEdfWQSFo5mjmaxsyF3lySLR3366HrgadKBESdEiuByCzhTd2KQNSlPNBbFKOT8B4scD1B0+/TqilT1dI3eFso+XcE9CDjJZBiDaVGMXDO22ZENeIj3HhdEuSgeA7oa0huOp7UItHMvGmWzmmx9MQkpBIvfprZntxSBCxHlOVl6mT1Q7C/wsGNBnIqUGNzA06nGcN4UL6SJi0qR6vVWv2mJjkUvEaehd1Vqac5MoDBw86Ta6wZhrFNaaj7UQBDWVf1QLFt64eNI/PNIYf8BwZ9kQFzK97LFv/7t75B8PzpW4dzOt2jTl6iR0p9d37oP3pGRyUOfJqJw1vvmbhvxh3PnlaCWAgobd0XC3/DuOgTDRlO/VQRHGa6pjbyT2AQzELrf0YS85WtGqRlW4oQKMl/b2LPRFNOkDw4DDN6cLwQ9J02b//6GOnlXVW2YlsTt+JZY3qKHY4//xby61GxIdhfPBn/v3fRupSR6V5gfSoDBmO/3nlxzk=
                    </dsig:Modulus>
                    <dsig:Exponent>AQAB</dsig:Exponent>
                </dsig:RSAKeyValue>
            </dsig:KeyValue>
        </dsig:KeyInfo>
    </dsig:Signature>
    <saml:Subject>
        <saml:NameID Format="urn:oasis:names:tc:SAML:2.0:nameid-format:persistent"
                     xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion">rhn-support-jshi
        </saml:NameID>
        <saml:SubjectConfirmation Method="urn:oasis:names:tc:SAML:2.0:cm:bearer">
            <saml:SubjectConfirmationData InResponseTo="_af50c689-dadd-4bb5-afc8-06ac1cc03208"
                                          NotOnOrAfter="2015-02-07T03:35:18.252Z"
                                          Recipient="https://ams-dev2.devlab.redhat.com/wapps/sso/consume.html"/>
        </saml:SubjectConfirmation>
    </saml:Subject>
    <saml:Conditions NotBefore="2015-02-07T03:35:07.052Z" NotOnOrAfter="2015-02-07T03:35:18.252Z">
        <saml:AudienceRestriction>
            <saml:Audience>https://ams-dev2.devlab.redhat.com/wapps/sso/</saml:Audience>
        </saml:AudienceRestriction>
    </saml:Conditions>
    <saml:AuthnStatement AuthnInstant="2015-02-07T03:35:09.053Z" SessionIndex="ID_7fdff6dc-eb03-44cb-8c56-bcd85c909a2d">
        <saml:AuthnContext>
            <saml:AuthnContextClassRef>urn:oasis:names:tc:SAML:2.0:ac:classes:Password</saml:AuthnContextClassRef>
        </saml:AuthnContext>
    </saml:AuthnStatement>
    <saml:AttributeStatement>
        <saml:Attribute Name="Role">
            <saml:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
                admin:org:all
            </saml:AttributeValue>
        </saml:Attribute>
        <saml:Attribute Name="Role">
            <saml:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
                authenticated
            </saml:AttributeValue>
        </saml:Attribute>
        <saml:Attribute Name="Role">
            <saml:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
                idp_authenticated
            </saml:AttributeValue>
        </saml:Attribute>
        <saml:Attribute Name="Role">
            <saml:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
                redhat:employees
            </saml:AttributeValue>
        </saml:Attribute>
    </saml:AttributeStatement>
</saml:Assertion>
</samlp:Response>
         */
        System.out.println(samlResponse);
        Map<String, NewCookie> cookies3 = resp3.getCookies();

        final String RH_USER = "rh_user";
        final String RH_LOCALE = "rh_locale";

        String rhUserVal = cookies3.get(RH_USER).getValue();
        String rhLocaleVal = cookies3.get(RH_LOCALE).getValue();
        System.out.println(rhUserVal);
        System.out.println(rhLocaleVal);
        client3.close();

        // step 4

        Client client4 = ClientBuilder.newBuilder()
                .sslContext(sslContext)
                .build();

        WebTarget target4 = client4.target("https://ams-dev2.devlab.redhat.com/wapps/sso/consume.html");

        Form samlResponseForm = new Form().param("SAMLResponse", samlResponse).param("RelayState", RelayState);

        Response resp4 = target4.request()
                .cookie(JSESSIONID, amsJSessionIdVal)
                .cookie(RH_USER, rhUserVal)
                .post(Entity.form(samlResponseForm));

        generateDebugScript(amsJSessionIdVal, samlResponse);

        // FIXME Server always reject our succeed SAMLResponse: An unexpected error happened while processing your request. <a href="login.html?redirect=/wapps/ugc/">Please try logging again</a>.
        // TODO need more error info from server side
        dumpCookies(resp4);
        final String RH_SSO = "rh_sso";
        // TODO get RH_SSO

        client4.close();

        // step 5
        Client client5 = ClientBuilder.newBuilder()
                .sslContext(sslContext)
                .build();

        WebTarget target5 = client5.target("https://access.devgssci.devlab.phx1.redhat.com/labs/jbossdvsat/application/recommendations?start_at=1422783710&end_at=1423215710&pass_code=2d619d7805814b1721ebb7c6e9b3ca61");

        Response resp5 = target5.request()
                .cookie(JSESSIONID, jSessionIdVal)
                .cookie(JSESSIONIDSSO, jSessionIdSsoVal)
                .cookie(RH_SSO, "FIXME: SHOULD GET FROM STEP4")
                .cookie(RH_USER, rhUserVal)
                .cookie(RH_LOCALE, rhLocaleVal).get();
        dumpCookies(resp5);

        client5.close();
    }

    private static void generateDebugScript(String amsJSessionIdVal, String samlResponse) throws Exception {
        String cmd = "curl -v -X POST -k 'https://ams-dev2.devlab.redhat.com/wapps/sso/consume.html' -c 'JSESSIONID=jSessionId; rh_user=rhn-support-jshi|Jingjing|B|; rh_locale=en_US' -d 'SAMLResponse=samlResponse'"
                .replaceAll("jSessionId", amsJSessionIdVal)
                .replaceAll("samlResponse", samlResponse);

        PrintWriter writer = new PrintWriter("/tmp/cmd.sh", "UTF-8");
        writer.println(cmd);
        writer.close();

        /*
        curl -v -X POST -k 'https://ams-dev2.devlab.redhat.com/wapps/sso/consume.html' -c 'JSESSIONID=FTk1ZNewwwYq+aFUpaRROEnV.8934cfe7; rh_user=rhn-support-jshi|Jingjing|B|; rh_locale=en_US' -d 'SAMLResponse=PHNhbWxwOlJlc3BvbnNlIHhtbG5zOnNhbWxwPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6cHJvdG9jb2wiIHhtbG5zOnNhbWw9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iIERlc3RpbmF0aW9uPSJodHRwczovL2Ftcy1kZXYyLmRldmxhYi5yZWRoYXQuY29tL3dhcHBzL3Nzby9jb25zdW1lLmh0bWwiIElEPSJJRF9lYzUxMjY3Yy02M2E1LTQzZTgtYmJhOS01YzdmODk3NzFlY2IiIEluUmVzcG9uc2VUbz0iX2FmNTBjNjg5LWRhZGQtNGJiNS1hZmM4LTA2YWMxY2MwMzIwOCIgSXNzdWVJbnN0YW50PSIyMDE1LTAyLTA3VDA0OjE4OjI3LjQ2NloiIFZlcnNpb249IjIuMCI+PHNhbWw6SXNzdWVyIHhtbG5zOnNhbWw9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iPmh0dHBzOi8vaWRwLmRldjIucmVkaGF0LmNvbS9pZHAvPC9zYW1sOklzc3Vlcj48ZHNpZzpTaWduYXR1cmUgeG1sbnM6ZHNpZz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnIyI+PGRzaWc6U2lnbmVkSW5mbz48ZHNpZzpDYW5vbmljYWxpemF0aW9uTWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIi8+PGRzaWc6U2lnbmF0dXJlTWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnI3JzYS1zaGExIi8+PGRzaWc6UmVmZXJlbmNlIFVSST0iI0lEX2VjNTEyNjdjLTYzYTUtNDNlOC1iYmE5LTVjN2Y4OTc3MWVjYiI+PGRzaWc6VHJhbnNmb3Jtcz48ZHNpZzpUcmFuc2Zvcm0gQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjZW52ZWxvcGVkLXNpZ25hdHVyZSIvPjxkc2lnOlRyYW5zZm9ybSBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMTAveG1sLWV4Yy1jMTRuIyIvPjwvZHNpZzpUcmFuc2Zvcm1zPjxkc2lnOkRpZ2VzdE1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyNzaGExIi8+PGRzaWc6RGlnZXN0VmFsdWU+alcvSEJvdW5aL2ZOMytvZUo5amIrYnFMaFRFPTwvZHNpZzpEaWdlc3RWYWx1ZT48L2RzaWc6UmVmZXJlbmNlPjwvZHNpZzpTaWduZWRJbmZvPjxkc2lnOlNpZ25hdHVyZVZhbHVlPk0remw4QWozbnExT3NNbGsyazFOb0hvSHc3aEZGb3BaSmxxNXZBUW9ubHpRNzRCVUltejB6b2ErNVRqRGFtY2p2Q1VxQTArTDQwK3ZoZHhHRFZHVmRDckhhem1xazB2dytuelBWWXloYWxEanJaS0lWdEIxUjRpZldXRWFITWt4RUg5Zjd6bFdTNGYybU9Qb1cxN2VocnA2YXNiTndpUXF1S2hneWxqNUpSMlJabGhsY2NvRUVPcjh6RXdVa2tCMlpTS0FlbldHVTRSc1pCRU11Qmx0YTd1Zmd1V2c3VjNDSDJzQUptV3MxUm9TdjVURWVBMW5DMGhob0JvV3FqVnBEVU1aUGNwOUNIazZPaHdTcFlzQlUyWW9HVXNUdit3TzJWZkRZMENQeU52dUhadGRVejM3bFJleTV1SHBPdThDb3cvZFlQL2NqSjFaZE5KeTZVZU1sVUJSUDJVZ0k5aGhONE9xS1dBV1JMSDhFbmsrOVNPTVVCK2psTHpZVlpjdmNsZ1d2dHRJV1dCc3RFWU14NEtNbHI4SUZuV3N3VkozWERiemY1K08zK2xnc0t5Q3RRTDhrQk1FdHIrNnJWS3FLZnBDQ1ByQ3BjMGs4bVZraitTNHlkcm8wZGFyWUoyNWRPWmtrT1FyeDhwaEo5WkY2bllHc242NytTWENRM2hQZUdHaC95bE1aVGNCSTQwZ0ZNaThCS1lSaUpKSnRiOEFvbHg1Q0E1NUt1S1NPVDBDOEVOeVRTN3dOMERyWGQzSUpFYXVvQjBVR0pTRURzbk1WZDJ0Y3JMbFUwNlFPdDBSQzZibXBCdVVqR0FVbXgvQlNaWlRYbjdMUXJRTmZobG9ZQ2V2QmJNUjJZNkJWdEpkcytjUmxMRSs5UURVMk5WdHIwUWFuY29INWVFPTwvZHNpZzpTaWduYXR1cmVWYWx1ZT48ZHNpZzpLZXlJbmZvPjxkc2lnOktleVZhbHVlPjxkc2lnOlJTQUtleVZhbHVlPjxkc2lnOk1vZHVsdXM+dWtoZEZ1eW9nZ2d3aHNEbG5MTlJkUjF2STFYUndhVTVQTEMrTkZlUFBoY2tMQ0xXUU82UjFKQUNZenY1ZDIrZ3hHbzVrTUxUZ1o4N082TE9udThiTGxQWHd6L0FkNE5Bc2tjQkd3UlZJMDkvT09TS2JlSVRmRjZYa0JrZnQwcFhNQTFEZWUwSHNTUHc5UHJjT0h1WGErZlNyOVFNTWkxK0tZWXVRMTlhUTZqSjEzOUtoUGJDYjg5V0VkZldRU0ZvNW1qbWF4c3lGM2x5U0xSMzM2NkhyZ2FkS0JFU2RFaXVCeUN6aFRkMktRTlNsUE5CYkZLT1Q4QjRzY0QxQjArL1RxaWxUMWRJM2VGc28rWGNFOUNEakpaQmlEYVZHTVhETzIyWkVOZUlqM0hoZEV1U2dlQTdvYTBodU9wN1VJdEhNdkdtV3ptbXg5TVFrcEJJdmZwclpudHhTQkN4SGxPVmw2bVQxUTdDL3dzR05CbklxVUdOekEwNm5HY040VUw2U0ppMHFSNnZWV3YybUpqa1V2RWFlaGQxVnFhYzVNb0RCdzg2VGE2d1pockZOYWFqN1VRQkRXVmYxUUxGdDY0ZU5JL1BOSVlmOEJ3WjlrUUZ6Szk3TEZ2Lzd0NzVCOFB6cFc0ZHpPdDJqVGw2aVIwcDlkMzdvUDNwR1J5VU9mSnFKdzF2dm1iaHZ4aDNQbmxhQ1dBZ29iZDBYQzMvRHVPZ1REUmxPL1ZRUkhHYTZwamJ5VDJBUXpFTHJmMFlTODVXdEdxUmxXNG9RS01sL2IyTFBSRk5Pa0R3NERETjZjTHdROUowMmIvLzZHT25sWFZXMllsc1R0K0paWTNxS0hZNC8veGJ5NjFHeElkaGZQQm4vdjNmUnVwU1I2VjVnZlNvREJtTy8zbmx4ems9PC9kc2lnOk1vZHVsdXM+PGRzaWc6RXhwb25lbnQ+QVFBQjwvZHNpZzpFeHBvbmVudD48L2RzaWc6UlNBS2V5VmFsdWU+PC9kc2lnOktleVZhbHVlPjwvZHNpZzpLZXlJbmZvPjwvZHNpZzpTaWduYXR1cmU+PHNhbWxwOlN0YXR1cz48c2FtbHA6U3RhdHVzQ29kZSBWYWx1ZT0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOnN0YXR1czpTdWNjZXNzIi8+PC9zYW1scDpTdGF0dXM+PHNhbWw6QXNzZXJ0aW9uIHhtbG5zPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXNzZXJ0aW9uIiBJRD0iSURfNDIxMzMyMGEtYWM1ZC00YzM2LTkyZDEtYjk0YWNkOWY4ODNmIiBJc3N1ZUluc3RhbnQ9IjIwMTUtMDItMDdUMDQ6MTg6MjcuNDY2WiIgVmVyc2lvbj0iMi4wIiB4bWxuczpzYW1sPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXNzZXJ0aW9uIj48c2FtbDpJc3N1ZXIgeG1sbnM6c2FtbD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFzc2VydGlvbiI+aHR0cHM6Ly9pZHAuZGV2Mi5yZWRoYXQuY29tL2lkcC88L3NhbWw6SXNzdWVyPjxkc2lnOlNpZ25hdHVyZSB4bWxuczpkc2lnPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjIj48ZHNpZzpTaWduZWRJbmZvPjxkc2lnOkNhbm9uaWNhbGl6YXRpb25NZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzEwL3htbC1leGMtYzE0biMiLz48ZHNpZzpTaWduYXR1cmVNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjcnNhLXNoYTEiLz48ZHNpZzpSZWZlcmVuY2UgVVJJPSIjSURfNDIxMzMyMGEtYWM1ZC00YzM2LTkyZDEtYjk0YWNkOWY4ODNmIj48ZHNpZzpUcmFuc2Zvcm1zPjxkc2lnOlRyYW5zZm9ybSBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyNlbnZlbG9wZWQtc2lnbmF0dXJlIi8+PGRzaWc6VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIi8+PC9kc2lnOlRyYW5zZm9ybXM+PGRzaWc6RGlnZXN0TWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnI3NoYTEiLz48ZHNpZzpEaWdlc3RWYWx1ZT5SNVBaSElBbkJrVnh3MURaV200NWd1N2MzdHc9PC9kc2lnOkRpZ2VzdFZhbHVlPjwvZHNpZzpSZWZlcmVuY2U+PC9kc2lnOlNpZ25lZEluZm8+PGRzaWc6U2lnbmF0dXJlVmFsdWU+dU9PMm1KdmxPTStzNHlKeW12WStxaTM3WkcxYXgwdHhackFPUm03cUgwL2FTMzFrcTd5TENOSGpjTTNwaG1tMTF4ZGcrL0tlN09kdU41WFdIcmRaakxRQ1pGQTNKVk5MVE9MK3Vua05RSitkUE1PWTVseVMzRmY1bGV6Mng2a2lmMllZU1kwaUw5ZStLdGl3ZlRrd0NxeUR6MEFoUGNMQUhYTElRRFpzZ1hYcGxnTXhIWGRCQ1ZkVFcreWozd1BXdUttR1V4c2tmSjRjaW5UYlJxY2FqUGxUUlJEQW5SSEsxWm5IV09jZFJzM1VGdVhhQWx5V1NpcjhHTkhXQktJZ1U0UnluSE5HSFhwdjZJajcySDd4SFVZV1RPTG5FUzk2NEErVk1wbE95RDhiQVhqMDdMRGlkWURhTWk5cWhpQy9BT1dKUVkySytZR05vdFBLUXAvM3A4eEtJMUw1NlV0U1dlZTZzL0dVUDFlU3IwZ1FmZWNub2Yvdnd5dktlN0ZvQzNUelpkTVFZTFJlL2E0UEVTVU5vaTBVeFRaTXgvRjU1SDZwNHljajhidlNBSnlZak0rU0M4N3Vmbk1mRWl4eU5JTHd4VE5kYnY4S1pTY05UVHYrenpxVnhWdmZkWTBIckFnRm91a0JMc0NETEpNeU5JU0MrWFpVbkYwckxxNDVXM0Y1NnNHN2ozaFNvQjJPdmVZT0t5akliZlBJZm5FcTNEV2JQVFpkeEdDQWMyU1lkNlBtREQ4RElCQWNpRWxvSkhmYlJHT0NUcUdyRGVycFd1SkIrMEFZQ0g4bG9wVmErT0lsRUxMRlloM3E4Q1BtWE5CUkpMdDFGNE9QZjFNSmR6TWFreVBDRy9qUjNHRGxyUlR4L2w0TlNtaHMyalhaUzJhUWRGWWxibzg9PC9kc2lnOlNpZ25hdHVyZVZhbHVlPjxkc2lnOktleUluZm8+PGRzaWc6S2V5VmFsdWU+PGRzaWc6UlNBS2V5VmFsdWU+PGRzaWc6TW9kdWx1cz51a2hkRnV5b2dnZ3doc0RsbkxOUmRSMXZJMVhSd2FVNVBMQytORmVQUGhja0xDTFdRTzZSMUpBQ1l6djVkMitneEdvNWtNTFRnWjg3TzZMT251OGJMbFBYd3ovQWQ0TkFza2NCR3dSVkkwOS9PT1NLYmVJVGZGNlhrQmtmdDBwWE1BMURlZTBIc1NQdzlQcmNPSHVYYStmU3I5UU1NaTErS1lZdVExOWFRNmpKMTM5S2hQYkNiODlXRWRmV1FTRm81bWptYXhzeUYzbHlTTFIzMzY2SHJnYWRLQkVTZEVpdUJ5Q3poVGQyS1FOU2xQTkJiRktPVDhCNHNjRDFCMCsvVHFpbFQxZEkzZUZzbytYY0U5Q0RqSlpCaURhVkdNWERPMjJaRU5lSWozSGhkRXVTZ2VBN29hMGh1T3A3VUl0SE12R21Xem1teDlNUWtwQkl2ZnByWm50eFNCQ3hIbE9WbDZtVDFRN0Mvd3NHTkJuSXFVR056QTA2bkdjTjRVTDZTSmkwcVI2dlZXdjJtSmprVXZFYWVoZDFWcWFjNU1vREJ3ODZUYTZ3WmhyRk5hYWo3VVFCRFdWZjFRTEZ0NjRlTkkvUE5JWWY4QndaOWtRRnpLOTdMRnYvN3Q3NUI4UHpwVzRkek90MmpUbDZpUjBwOWQzN29QM3BHUnlVT2ZKcUp3MXZ2bWJodnhoM1BubGFDV0Fnb2JkMFhDMy9EdU9nVERSbE8vVlFSSEdhNnBqYnlUMkFRekVMcmYwWVM4NVd0R3FSbFc0b1FLTWwvYjJMUFJGTk9rRHc0RERONmNMd1E5SjAyYi8vNkdPbmxYVlcyWWxzVHQrSlpZM3FLSFk0Ly94Ynk2MUd4SWRoZlBCbi92M2ZSdXBTUjZWNWdmU29EQm1PLzNubHh6az08L2RzaWc6TW9kdWx1cz48ZHNpZzpFeHBvbmVudD5BUUFCPC9kc2lnOkV4cG9uZW50PjwvZHNpZzpSU0FLZXlWYWx1ZT48L2RzaWc6S2V5VmFsdWU+PC9kc2lnOktleUluZm8+PC9kc2lnOlNpZ25hdHVyZT48c2FtbDpTdWJqZWN0PjxzYW1sOk5hbWVJRCBGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpuYW1laWQtZm9ybWF0OnBlcnNpc3RlbnQiIHhtbG5zOnNhbWw9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iPnJobi1zdXBwb3J0LWpzaGk8L3NhbWw6TmFtZUlEPjxzYW1sOlN1YmplY3RDb25maXJtYXRpb24gTWV0aG9kPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6Y206YmVhcmVyIj48c2FtbDpTdWJqZWN0Q29uZmlybWF0aW9uRGF0YSBJblJlc3BvbnNlVG89Il9hZjUwYzY4OS1kYWRkLTRiYjUtYWZjOC0wNmFjMWNjMDMyMDgiIE5vdE9uT3JBZnRlcj0iMjAxNS0wMi0wN1QwNDoxODozNi42NjZaIiBSZWNpcGllbnQ9Imh0dHBzOi8vYW1zLWRldjIuZGV2bGFiLnJlZGhhdC5jb20vd2FwcHMvc3NvL2NvbnN1bWUuaHRtbCIvPjwvc2FtbDpTdWJqZWN0Q29uZmlybWF0aW9uPjwvc2FtbDpTdWJqZWN0PjxzYW1sOkNvbmRpdGlvbnMgTm90QmVmb3JlPSIyMDE1LTAyLTA3VDA0OjE4OjI1LjQ2NloiIE5vdE9uT3JBZnRlcj0iMjAxNS0wMi0wN1QwNDoxODozNi42NjZaIj48c2FtbDpBdWRpZW5jZVJlc3RyaWN0aW9uPjxzYW1sOkF1ZGllbmNlPmh0dHBzOi8vYW1zLWRldjIuZGV2bGFiLnJlZGhhdC5jb20vd2FwcHMvc3NvLzwvc2FtbDpBdWRpZW5jZT48L3NhbWw6QXVkaWVuY2VSZXN0cmljdGlvbj48L3NhbWw6Q29uZGl0aW9ucz48c2FtbDpBdXRoblN0YXRlbWVudCBBdXRobkluc3RhbnQ9IjIwMTUtMDItMDdUMDQ6MTg6MjcuNDY2WiIgU2Vzc2lvbkluZGV4PSJJRF80MjEzMzIwYS1hYzVkLTRjMzYtOTJkMS1iOTRhY2Q5Zjg4M2YiPjxzYW1sOkF1dGhuQ29udGV4dD48c2FtbDpBdXRobkNvbnRleHRDbGFzc1JlZj51cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YWM6Y2xhc3NlczpQYXNzd29yZDwvc2FtbDpBdXRobkNvbnRleHRDbGFzc1JlZj48L3NhbWw6QXV0aG5Db250ZXh0Pjwvc2FtbDpBdXRoblN0YXRlbWVudD48c2FtbDpBdHRyaWJ1dGVTdGF0ZW1lbnQ+PHNhbWw6QXR0cmlidXRlIE5hbWU9IlJvbGUiPjxzYW1sOkF0dHJpYnV0ZVZhbHVlIHhtbG5zOnhzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYSIgeG1sbnM6eHNpPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYS1pbnN0YW5jZSIgeHNpOnR5cGU9InhzOnN0cmluZyI+YWRtaW46b3JnOmFsbDwvc2FtbDpBdHRyaWJ1dGVWYWx1ZT48L3NhbWw6QXR0cmlidXRlPjxzYW1sOkF0dHJpYnV0ZSBOYW1lPSJSb2xlIj48c2FtbDpBdHRyaWJ1dGVWYWx1ZSB4bWxuczp4cz0iaHR0cDovL3d3dy53My5vcmcvMjAwMS9YTUxTY2hlbWEiIHhtbG5zOnhzaT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS9YTUxTY2hlbWEtaW5zdGFuY2UiIHhzaTp0eXBlPSJ4czpzdHJpbmciPmF1dGhlbnRpY2F0ZWQ8L3NhbWw6QXR0cmlidXRlVmFsdWU+PC9zYW1sOkF0dHJpYnV0ZT48c2FtbDpBdHRyaWJ1dGUgTmFtZT0iUm9sZSI+PHNhbWw6QXR0cmlidXRlVmFsdWUgeG1sbnM6eHM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hIiB4bWxuczp4c2k9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hLWluc3RhbmNlIiB4c2k6dHlwZT0ieHM6c3RyaW5nIj5pZHBfYXV0aGVudGljYXRlZDwvc2FtbDpBdHRyaWJ1dGVWYWx1ZT48L3NhbWw6QXR0cmlidXRlPjxzYW1sOkF0dHJpYnV0ZSBOYW1lPSJSb2xlIj48c2FtbDpBdHRyaWJ1dGVWYWx1ZSB4bWxuczp4cz0iaHR0cDovL3d3dy53My5vcmcvMjAwMS9YTUxTY2hlbWEiIHhtbG5zOnhzaT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS9YTUxTY2hlbWEtaW5zdGFuY2UiIHhzaTp0eXBlPSJ4czpzdHJpbmciPnJlZGhhdDplbXBsb3llZXM8L3NhbWw6QXR0cmlidXRlVmFsdWU+PC9zYW1sOkF0dHJpYnV0ZT48L3NhbWw6QXR0cmlidXRlU3RhdGVtZW50Pjwvc2FtbDpBc3NlcnRpb24+PC9zYW1scDpSZXNwb25zZT4='
         */
    }

    private static void dumpCookies(Response resp) {
        System.out.println("\n+++++++++++++++");
        System.out.println("Status: " + resp.getStatus());
        System.out.println("Length: " + resp.getLength());

        try {
            for (Map.Entry entry : resp.getCookies().entrySet()) {
                System.out.println(entry.getValue());
            }
        } catch (NullPointerException e) {
        }

    }

}
