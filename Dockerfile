# on part d'une image incluant java et maven
#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
FROM maven:3.8.7-eclipse-temurin-17-alpine
# on copie les sources de notre projet dans l'image
COPY src src
# on copie aussi le pom.xml
COPY pom.xml .
# on construit le projet
RUN mvn package
#on lance le programme construit au démarrage du conteneur
CMD java -jar target/*.jar
#On documente l'utilisation du port 8787
EXPOSE 8989
