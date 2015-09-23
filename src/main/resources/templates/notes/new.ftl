<html>
<head>
    <title>New Note</title>
</head>

<body>
<h1>New Note</h1>

<form action="/notes" method="POST">
    <dl>
        <dt>Title</dt>
        <dd>
            <input type="text" name="title">
        </dd>

        <dt>Content</dt>
        <dd>
            <textarea type="text" name="content" rows="8" cols="80"></textarea>
        </dd>
        <dt><input type="submit" value="Create" /></dt>
    </dl>
</form>
</body>
</html>