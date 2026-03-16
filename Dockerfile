# Imagen base de Java
FROM eclipse-temurin:17-jdk-jammy

# Directorio dentro del contenedor
WORKDIR /app

# Copiar el jar
COPY target/taskflow-0.0.1-SNAPSHOT.jar app.jar

# Puerto de la aplicación
EXPOSE 8080

# Ejecutar aplicación
ENTRYPOINT ["java","-jar","app.jar"]