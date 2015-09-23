package demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class NotesController {
    @Autowired
    private NotesRepository notesRepository;

    @RequestMapping(value = "/notes", method = RequestMethod.GET)
    public String notesIndex(Model model) {
        model.addAttribute("notes", notesRepository.findAll());
        return "notes/index";
    }

    @RequestMapping(value = "/notes/new", method = RequestMethod.GET)
    public String notesNew() {
        return "notes/new";
    }

    @RequestMapping(value = "/notes", method = RequestMethod.POST)
    public String notesCreate(Note note) {
        note = notesRepository.save(note);

        return "redirect:/notes/" + note.getId();
    }

    @RequestMapping(value = "/notes/{noteId}", method = RequestMethod.GET)
    public String notesShow(@PathVariable("noteId") Integer noteId, Model model) {
        Note note = notesRepository.findOne(noteId);
        model.addAttribute("note", note);

        return "notes/show";
    }
}
