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
    </body>
</html>
