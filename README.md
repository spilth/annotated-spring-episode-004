# CRUD Web App: Part 1 - Create & Read

## Introduction

We're going to build a simple note taking web app that lets us create, view, list and edit notes written using the popular [Markdown](http://daringfireball.net/projects/markdown/) syntax. In this first part we're going to implement Creating and Reading. In a future episode we'll add Editing and deletion.

## Project Initialization

We start by creating a Spring Boot web app with the 'data-jpa', 'freemarker' and 'h2' dependencies.

    $ spring init crud -d=web,data-jpa,freemarker,h2

We'll be using [Spring Data JPA](http://projects.spring.io/spring-data-jpa/) to help us create a Note model that can be easily persisted and queried.

To keep things simple we're going to use the [H2 in-memory database engine](http://www.h2database.com/) so that we can focus on just the model, controller and view. In a future episode we'll configure the application to use MySQL instead.

The template engine we'll be using is [Freemarker](http://freemarker.org).

## CRUD URL Convention

We're going to follow [a popular convention for the URLs and HTTP verbs that we'll use](http://microformats.org/wiki/rest/urls) in our controller:

| Path             | HTTP Verb | Controller Method | Purpose                                  |
|------------------|-----------|-------------------|------------------------------------------|
| /notes           | GET       | index             | List all Notes                           |
| /notes/new       | GET       | new               | Render form for creating a new Note      |
| /notes           | POST      | create            | Create a new Note                        |
| /notes/{id}      | GET       | show              | Display a specific Note                  |

- We list all notes by GETting `/notes`
- We load the form for a new note by GETting `/notes/new`
- We create a new note by POSTing a note's attributes to `/notes`
- We show a note by GETtting `/notes/{id}`, where `id` is its unique identifier - generally a primary key in the model's database table

## Main Page

To start, we’re going to make a page that will eventually list all our notes. But since we don't have any to list yet, we'll just have a button that lets us make a new note.

First we make a controller for our notes with an index method that is mapped to `/notes`:

    @Controller
    public class NotesController {
        @RequestMapping("/notes", RequestMethod = GET)
        public String notesIndex() {
            return "notes/index";
        }
    }

Now we make the template `index.ftl` inside `src/resources/templates/notes/`:

    <html>
        <head>
            <title>Notes</title>
        </head>

        <body>
            <h1>Notes</h1>

            <p><a href="/notes/new">New Note</a></p>
        </body>
    </html>

Let's add a controller method to handle the path in our link: `/notes/new`.

    @RequestMapping("/notes/new")
    public String notesNew() {
        return "notes/new";
    }

Then inside `src/resources/templates/notes/`, create `new.ftl` with a form for entering a Note's title and contents:

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

In order to save the Note, we'll need two things: a class that represents a Note and a repository that handles saving Notes.

The Note model will have 3 fields: id, title and content. We also need to add getters and setters for each field.

    public class Note {
        private Integer id;

        private String title;

        private String content;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

Spring Data JPA provides a `CrudRepository` class that we can extend to easily create a repository for our Notes. We pass it the class we want to store and the type we are using for the `id` field.  This interface will be picked up by Spring and automatically have methods for finding and saving Notes.

    public interface NotesRepository extends CrudRepository<Note, Integer>{}

When you click the submit button the form will `POST` to `/notes`. Let's add a controller method to handle that.

    @Controller
    public class NotesController {
        @Autowired
        private NotesRepository notesRepository;

        @RequestMapping(value = "/notes", method = POST)
        public String notesCreate(Note note) {
            note = notesRepository.save(note);

            return "redirect:/notes/" + note.getId();
        }
    }

The last line of the create method redirects the user to the show page of the Note once it has been created. We'll need one last controller method to handle that.

    @RequestMapping(value = "/notes/{noteId}", method = GET)
    public String notesShow(@PathVariable("noteId") Integer noteId, Model model) {
        Note note = notesRepository.findOne(noteId);
        model.addAttribute("note", note);

        return "notes/show";
    }

Here we are passing the `id` of the note in the path to the method. Then inside the method we can use the repository to find the requested note. Finaly, we assign this note to a model that is automatically made available to the view template.

We create `show.ftl` inside of `src/resouces/templates/notes/` and give if the following contents:

    <html>
        <head>
            <title>${note.title}</title>
        </head>

        <body>
            <h1>${note.title}</h1>

            <p>${note.content}</p>

            <p><a href="/notes/">All Notes</a></p>
        </body>
    </html>

If we start the web application, we get an error during startup:

    Error creating bean with name 'notesRepository': Invocation of init method failed; nested exception is java.lang.IllegalArgumentException: Not an managed type: class demo.notes.Note

It's complaining that our class Note is `Not an managed type`.

We need to put some annotations on the Note class so it knows that it's a model we can save. We add the `@Entity` annotation to the class to mark it as a Data entity. We then add the `@Id` annotation to the `id` field to mark it as the primary key for this class, along with `@GeneratedValue` to auto-populate the id on creation. Finally, we need to use the `@Lob` annotation to specify that the `content` field will hold a large amount of data. Otherwise we'd be limited to 255 characters by default.

    @Entity
    public class Note {
        @Id
        @GeneratedValue
        private Integer id;

        @Lob
        private String content;

If we restart the server, we should be able to create a new note and view it. But there's still no way to still all the notes in the sytem.

## List Notes Revisited

We need to update the index action to load all the Notes in the repository and expose that list to the view model:

    @RequestMapping(value = "/notes", method = GET)
    public String notesIndex(Model model) {
        model.addAttribute("notes", notesRepository.findAll());

        return "notes/index";
    }

Then we can update `index.ftl` to loop through the notes and render a link to each one.

    <ul>
        <#list notes as note>
            <li><a href="/notes/${note.id}">${note.title}</a></li>
        </#list>
    </ul>

## Adding Markdown Support to Notes

The last piece of functionality we need to add is supporting Markdown in the contents of our notes. There is a Java library named [pegdown](https://github.com/sirthias/pegdown) that helps us quickly accomplish this.

First we add it as a dependency to our project in out `pom.xml`:

    <dependency>
        <groupId>org.pegdown</groupId>
        <artifactId>pegdown</artifactId>
        <version>1.5.0</version>
    </dependency>

Then we add a getter method in our Note class:

    public String getContentHtml() {
        return new PegDownProcessor().markdownToHtml(content);
    }

And call it from the show template:

    <html>
        <head>
            <title>${note.title}</title>
        </head>

        <body>
            <h1>${note.title}</h1>

            <p>${note.contentHtml}</p>

            <p><a href="/notes/">All Notes</a></p>
        </body>
    </html>

Once again we restart our server, create a note, paste in some Markdown and voila - we can generate HTML from Markdown.

## Exercise

- Use what you learned in Episode 3 to apply a consistent look to the site using Bootstrap, WebJars and Freemarker Macros.
