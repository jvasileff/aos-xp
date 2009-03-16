<%@ page session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html>
    <body>
        <h1>adsf</h1>
        ${fn:endsWith('asdf', 'df') || fn:startsWith('asdf', 'a')}
        ${fn:length('asdf')}

        ${fn:endsWith('asdf', 'df')}
        ${fn:length('asdf')}

        <c:set var="myvar" value="myvalue"/>

        <hr>

        <c:url value="relative"/>
        <br/>
        <c:url value="/absolute"/>
        <br/>
        <c:url value="http://www.example.com/fullyQualified"/>
        <hr>
        <br/>
        <c:url value="/absolute" context="/mycontext"/>
        <br/>
        Session: ${pageContext.session}<br/>
        <jsp:useBean id="startDate"  class="java.util.Date"/>
        Date: <fmt:formatDate type="both" dateStyle="short" timeStyle="short" value="${startDate}"/><br/>
        Session: \{$pageContext.session}<br/>
        SessionVal: \{$sessionScope.someVal}<br/>
    </body>
</html>
