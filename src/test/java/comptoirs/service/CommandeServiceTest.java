package comptoirs.service;

import comptoirs.dao.CategorieRepository;
import comptoirs.dao.CommandeRepository;
import comptoirs.dao.ProduitRepository;
import comptoirs.entity.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
 // Ce test est basé sur le jeu de données dans "test_data.sql"
class CommandeServiceTest {
    private static final String ID_PETIT_CLIENT = "0COM";
    private static final String ID_GROS_CLIENT = "2COM";
    private static final String VILLE_PETIT_CLIENT = "Berlin";

    private static final Integer COMMANDE_NON_LIVREE = 99998;

    private static final Integer COMMANDE_LIVREE = 99999;
    private static final BigDecimal REMISE_POUR_GROS_CLIENT = new BigDecimal("0.15");

    //jeu de données pour test



    @Autowired
    private CommandeService service;
    @Autowired
    private CommandeRepository commandeDao;

    @Autowired
    private ProduitRepository produitDao;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) //Reinitialise la base de données après chaque test


    @Test
    @Order(4)
    void testCreerCommandePourGrosClient() {
        var commande = service.creerCommande(ID_GROS_CLIENT);
        assertNotNull(commande.getNumero(), "On doit avoir la clé de la commande");
        assertEquals(REMISE_POUR_GROS_CLIENT, commande.getRemise(),
            "Une remise de 15% doit être appliquée pour les gros clients");
    }

    @Test
    @Order(2)
    void testCreerCommandePourPetitClient() {
        var commande = service.creerCommande(ID_PETIT_CLIENT);
        assertNotNull(commande.getNumero());
        assertEquals(BigDecimal.ZERO, commande.getRemise(),
            "Aucune remise ne doit être appliquée pour les petits clients");
    }

    @Test
    @Order(3)
    void testCreerCommandeInitialiseAdresseLivraison() {
        var commande = service.creerCommande(ID_PETIT_CLIENT);
        assertEquals(VILLE_PETIT_CLIENT, commande.getAdresseLivraison().getVille(),
            "On doit recopier l'adresse du client dans l'adresse de livraison");
    }

    @Test
    @Order(1)
    void testEnregistreExpedition() {
       Optional<Commande> commandeNonLivree = commandeDao.findById(COMMANDE_NON_LIVREE);

        assertEquals(null, commandeNonLivree.get().getEnvoyeele(),
                "Aucune adresse de livraison");



        assertThrows(IllegalStateException.class, () -> service.enregistreExpedition(COMMANDE_LIVREE), "COMMANDE DEJA LIVREE");



    }



    @Test
    @Order(5) //Ce test doit être fait en dernier
    void testMiseAJourDynamiqueStock () {


        var produitAvecAncienStock = produitDao.findById(98).orElseThrow();// Le produit 98 a 27unités en stock au départ

        Commande c = service.enregistreExpedition(99998);//Une commande de 20 unités du produit 98

        var produitAvecStockMisAJour = produitDao.findById(98).orElseThrow();// le nouveau stock dans la base de données doit être décrémenté de 20

        assertEquals(produitAvecAncienStock.getUnitesEnStock() - 20, c.getLignes().get(0).getProduit().getUnitesEnStock(), "LE produit " +
                "n'a pas été décrémenté");


        assertEquals(produitAvecStockMisAJour.getUnitesEnStock() , c.getLignes().get(0).getProduit().getUnitesEnStock(), "Le stock doit être mis à jour dans" +
                "la base de données");
    }



}
