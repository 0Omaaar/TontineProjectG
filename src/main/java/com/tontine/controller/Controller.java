package com.tontine.controller;



import com.tontine.entities.DemandeJointure;
import com.tontine.entities.MembreTontine;
import com.tontine.entities.Tontine;
import com.tontine.entities.User;
import com.tontine.repository.DemandeJointureRepository;
import com.tontine.repository.TontineRepository;
import com.tontine.repository.UserRepository;
import com.tontine.service.DemandeJointureService;
import com.tontine.service.TontineService;
//import com.tontine.service.TontineServiceImp;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;


@RestController
//@org.springframework.stereotype.Controller
//@ResponseBody
@RequestMapping
public class Controller {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TontineRepository tontineRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TontineService tontineService;

    @Autowired
    private DemandeJointureRepository demandeJointureRepository;

    @GetMapping("/")
    public ModelAndView index(Model model, Authentication authentication
                            , @RequestParam(name="nom", defaultValue = "") String nom
                            , @RequestParam(name ="frequence", defaultValue = "")   String frequence
                            , @RequestParam(name = "prixMin", defaultValue = "0") int prixMin
                            , @RequestParam(name = "prixMax", defaultValue = "100000") int prixMax){
        ModelAndView modelAndView = new ModelAndView();
        List<Tontine> tontines = tontineRepository.findByNomContains(nom);

        if (!frequence.isEmpty() || prixMin > 0 || prixMax < 100000) {
            if(!frequence.isEmpty()){
                tontines = tontines.stream()
                        .filter(tontine -> tontine.getFrequence().name().equalsIgnoreCase(frequence))
                        .filter(tontine -> tontine.getMontantPeriode() >= prixMin && tontine.getMontantPeriode() <= prixMax)
                        .collect(Collectors.toList());
            }
            else {
                tontines = tontines.stream()
                        .filter(tontine -> tontine.getMontantPeriode() >= prixMin && tontine.getMontantPeriode() <= prixMax)
                        .collect(Collectors.toList());
            }
            model.addAttribute("prixMin", prixMin);
            model.addAttribute("prixMax", prixMax);
        }

        int user_id = 0;
        User authenticated_user = null;

        if(authentication != null){
            authenticated_user = userRepository.findByEmail(getLoggedInUserDetails().getUsername()).orElse(null);
            List<MembreTontine> membreTontines= authenticated_user.getMembreTontines();
            model.addAttribute("membres", membreTontines);
            model.addAttribute("user", authenticated_user);
        }

        if(authenticated_user != null){
            user_id = authenticated_user.getId();
        }

        //display the "annuler la demande" button
//        List<DemandeJointure> demandeJointures = demandeJointureRepository.findAll();
//        model.addAttribute("demandesJointures",demandeJointures);

        model.addAttribute("tontines", tontines);
        model.addAttribute("user_id", user_id);
        model.addAttribute("nom", nom);
        modelAndView.setViewName("home");
        return modelAndView;
//        return tontines;
    }


    @GetMapping("/afficherMembresTontine/{id}")
    public ModelAndView membresTontine(Model model, Authentication authentication,
                                       @PathVariable(name = "id") int id){
        int user_id = 0;
        User authenticated_user = null;

        if(authentication != null){
            authenticated_user = userRepository.findByEmail(getLoggedInUserDetails().getUsername()).orElse(null);
            List<MembreTontine> membreTontines= authenticated_user.getMembreTontines();
            model.addAttribute("membres", membreTontines);
            model.addAttribute("user", authenticated_user);
        }

        if(authenticated_user != null){
            user_id = authenticated_user.getId();
        }

        Tontine tontine = tontineService.findById(id);
        if(tontine != null){
            List<MembreTontine> membreTontines = (List<MembreTontine>) tontine.getMembreTontines();
            model.addAttribute("membres", membreTontines);
        }

        model.addAttribute("user_id", user_id);

        return new ModelAndView("infosMembresTontine");
    }

    @PostMapping("/updateProfile")
    public ModelAndView updateProfile(Authentication authentication, User user, Model model,
                                      RedirectAttributes redirectAttributes){
        ModelAndView modelAndView = new ModelAndView("redirect:/");
        try{
            User userFinded = userRepository.findByEmail(getLoggedInUserDetails().getUsername()).orElse(null);
            userFinded.setCin(user.getCin());
            userFinded.setEmail(user.getEmail());
            userFinded.setNom_prenom(user.getNom_prenom());
            userFinded.setNumTele(user.getNumTele());
            userRepository.save(userFinded);
            redirectAttributes.addFlashAttribute("successMessage", "Votre profil est modifié avec succès");
        }catch(Exception e){
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("dangerMessage", "Une Erreur est survenue lors de modification de votre profil.");
        }

        return modelAndView;
    }

    @GetMapping("/login")
    public ModelAndView login(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        return modelAndView;
    }

    @GetMapping("/register")
    public ModelAndView register(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("register");
        return modelAndView;
    }

    @PostMapping("/user/save")
    public ModelAndView saveUSer(@ModelAttribute("User") @Valid User ourUser,
                                 RedirectAttributes redirectAttributes,
                                 BindingResult bindingResult){
        try{
            if(bindingResult.hasErrors()){
                redirectAttributes.addFlashAttribute("dangerMessage", "Une Erreur est survenue lors de création de votre Compte.");
                return new ModelAndView("redirect:/register");
            }
            ourUser.setPassword(passwordEncoder.encode(ourUser.getPassword()));
            User result = userRepository.save(ourUser);
            redirectAttributes.addFlashAttribute("successMessage", "Votre Compte est crée avec succès");

        }catch (Exception e){
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("dangerMessage", "Une Erreur est survenue lors de création de votre Compte.");
        }

        ModelAndView modelAndView = new ModelAndView("redirect:/login");

        return modelAndView;
    }
    @GetMapping("/users/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Object> getAllUSers(){
        return ResponseEntity.ok(userRepository.findAll());
    }
    @GetMapping("/users/single")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    public ResponseEntity<Object> getMyDetails(){
        return ResponseEntity.ok(userRepository.findByEmail(getLoggedInUserDetails().getUsername()));
    }

    public UserDetails getLoggedInUserDetails(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.getPrincipal() instanceof UserDetails){
            return (UserDetails) authentication.getPrincipal();
        }
        return null;
    }
}
