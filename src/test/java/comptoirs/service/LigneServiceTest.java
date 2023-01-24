package comptoirs.service;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import comptoirs.dao.CommandeRepository;
import comptoirs.dao.LigneRepository;
import comptoirs.dao.ProduitRepository;
import comptoirs.entity.Commande;
import comptoirs.entity.Ligne;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.validation.ConstraintViolationException;
import org.springframework.test.annotation.DirtiesContext;

import java.util.LinkedList;
import java.util.NoSuchElementException;

@SpringBootTest
 // Ce test est basé sur le jeu de données dans "test_data.sql"
class LigneServiceTest {
    static final int NUMERO_COMMANDE_DEJA_LIVREE = 99999;
    static final int NUMERO_COMMANDE_PAS_LIVREE  = 99998;
    static final int REFERENCE_PRODUIT_DISPONIBLE_1 = 93;
    static final int REFERENCE_PRODUIT_DISPONIBLE_2 = 94;
    static final int REFERENCE_PRODUIT_DISPONIBLE_3 = 95;
    static final int REFERENCE_PRODUIT_DISPONIBLE_4 = 96;
    static final int REFERENCE_PRODUIT_INDISPONIBLE = 97;
    static final int UNITES_COMMANDEES_AVANT = 0;

    @Autowired
    LigneService service;

    @Autowired
    CommandeRepository commandeDto;

    @Autowired
    LigneRepository ligneDto;

    @Autowired
    ProduitRepository produitDto;


    @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) //Reinitialise la base de données après chaque test

    @Test
    void onPeutAjouterDesLignesSiPasLivre() {
        var ligne = service.ajouterLigne(NUMERO_COMMANDE_PAS_LIVREE, REFERENCE_PRODUIT_DISPONIBLE_1, 1);
        assertNotNull(ligne.getId(),
        "La ligne doit être enregistrée, sa clé générée"); 
    }

    @Test
    void laQuantiteEstPositive() {
        assertThrows(ConstraintViolationException.class, 
            () -> service.ajouterLigne(NUMERO_COMMANDE_PAS_LIVREE, REFERENCE_PRODUIT_DISPONIBLE_1, 0),
            "La quantite d'une ligne doit être positive");
    }

    @Test
    void ajoutNouvelleLigneDeCommande() {
        var nouvelleLigne = service.ajouterLigne(NUMERO_COMMANDE_PAS_LIVREE, REFERENCE_PRODUIT_DISPONIBLE_1, 1);
        nouvelleLigne = service.ajouterLigne(NUMERO_COMMANDE_PAS_LIVREE, REFERENCE_PRODUIT_DISPONIBLE_2, 1);

        int commandePasLivreeNbreLigne= commandeDto.findById(NUMERO_COMMANDE_PAS_LIVREE).orElseThrow().getLignes().size();//
        // La commande pas livrée contenait déjà une ligne de commande poue le produit 98

        assertEquals(1+1+1,
                commandeDto.findById(NUMERO_COMMANDE_PAS_LIVREE).orElseThrow().getLignes().size(), "Il " +
                "doiy y avoir 3 commandes"); //ne marche pas en lazy loading, fetch type doit être eager

    }

    @Test
    void leProduitReferenceExiste() {

        assertThrows(NoSuchElementException.class,
                () -> service.ajouterLigne(NUMERO_COMMANDE_PAS_LIVREE, 07, 10),
                "La produit référencé n'existe pas");
    }

    @Test
    void laCommandeExiste() {

        assertThrows(NoSuchElementException.class,
                () -> service.ajouterLigne(7, REFERENCE_PRODUIT_DISPONIBLE_1, 10),
                "La commande n'existe pas");
    }

    @Test
    void laCommandeNestPasEncoreExpeddiee() {

        assertThrows(IllegalStateException.class,
                () -> service.ajouterLigne(NUMERO_COMMANDE_DEJA_LIVREE, REFERENCE_PRODUIT_DISPONIBLE_1, 10),
                "La commande est déjà expédiée");
    }

    @Test
    void quantiteEnStockSuffisante() {

        assertThrows(IllegalArgumentException.class,
                () -> service.ajouterLigne(NUMERO_COMMANDE_PAS_LIVREE, REFERENCE_PRODUIT_DISPONIBLE_1, 10000),
                "Quantité insuffisante en stock");
    }


    @Test
    void testMiseAJourUniteCommandees() {
        int qteCommandees = produitDto.findById(REFERENCE_PRODUIT_DISPONIBLE_1).orElseThrow().getUnitesCommandees();
       Ligne ligne =  service.ajouterLigne(NUMERO_COMMANDE_PAS_LIVREE, REFERENCE_PRODUIT_DISPONIBLE_1, 20);
        assertEquals(qteCommandees+20,ligne.getProduit().getUnitesCommandees(),
                "quantité commandée doit être incrémentée");
    }







}
