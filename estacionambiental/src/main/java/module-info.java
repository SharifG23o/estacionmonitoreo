module co.edu.proyecto.estacionmonitoreo {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires java.net.http; // opcional si usas HTTP
    requires com.google.gson;

    

    // Abrir el paquete principal
    opens co.edu.proyecto.estacionmonitoreo to javafx.fxml;
    
    // IMPORTANTE: Abrir el paquete del controlador para FXML
    opens co.edu.proyecto.estacionmonitoreo.viewController to javafx.fxml;

    opens co.edu.proyecto.estacionmonitoreo.controller to ALLUNNAMED;

    
    // Exportar paquetes
    exports co.edu.proyecto.estacionmonitoreo;
    exports co.edu.proyecto.estacionmonitoreo.viewController;
    exports co.edu.proyecto.estacionmonitoreo.controller;
    exports co.edu.proyecto.estacionmonitoreo.model;
}