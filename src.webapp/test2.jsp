<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

        <jsp:useBean id="browser" class="org.anodyneos.servlet.util.BrowserDetector"/>
        <c:set target="${browser}" property="request" value="${pageContext.request}"/>
        <html>
            <head/>
            <body>
                <h1>Test XP Page</h1>
                <div>
                    browserName: ${browser.browserName}
                    <br/>
                    browserVersion: ${browser.browserVersion}
                    <br/>
                    browserVersionString: ${browser.browserVersionString}
                </div>
                <h1>Test AutoHtml</h1>
                    alksdjfal ksjaslkdfj alskfj alskfj aslfh alsdfh alskfh
                    alsdfh als aslkfh as jvasileff@marketingcentral.com ldfh
                    alskdfh alskdjfh alskdjfh alskdfh alskdjfh laskjdh flakjs

                    alkj shfalsdkfh asldjfh alsdjfh alskdfh alsfh alskfh as
                    lkjfhasldfkj hasldfh l

                <h1>Test Functions</h1>
                <pre>
\${fn:startsWith('asdf', 'a')} = ${fn:startsWith('asdf', 'a')}
\${fn:endsWith('asdf', 'b')} = ${fn:endsWith('asdf', 'b')}
\${fn:join(fn:split('a,b,c', ','), ';')} = ${fn:join(fn:split('a,b,c', ','), ';')}
                </pre>


                <h3>Request Parameters</h3>
                <table border="2">
                    <c:set var="last" value=""/>
                    <c:forEach var="current" items="${param}" varStatus="outerStatus">
                        <c:forEach var="aVal" items="${paramValues[current.key]}">
                            <tr>
                                <td>
                                    <c:if test="${last != current.key}">
                                        <b>${current.key}</b>
                                    </c:if>
                                </td>
                                <td>${aVal}</td>
                            </tr>
                            <c:set var="last" value="${current.key}"/>
                        </c:forEach>
                    </c:forEach>
                </table>

                <h3>Headers</h3>
                <table border="2">
                    <c:forEach var="current" items="${header}">
                        <tr>
                            <td>${current}</td>
                        </tr>
                    </c:forEach>
                </table>

                <h3>Cookies</h3>
                <table border="2">
                    <c:forEach var="current" items="${cookie}">
                        <tr>
                            <td><b>${current.value.name}</b></td>
                            <td>${current.value.value}</td>
                        </tr>
                    </c:forEach>
                </table>

                <div>URL Testing</div>
                <a>
                    Link #1
                </a>

                <div>Another URL Testing</div>
                <a>
                    Link #2
                </a>
            </body>
        </html>
