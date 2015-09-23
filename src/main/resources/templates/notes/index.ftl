<html>
<head>
    <title>Notes</title>
</head>

<body>
<h1>Notes</h1>

<ul>
<#list notes as note>
    <li><a href="/notes/${note.id}">${note.title}</a></li>
</#list>
</ul>

<p><a href="/notes/new">New Note</a></p>
</body>
</html>