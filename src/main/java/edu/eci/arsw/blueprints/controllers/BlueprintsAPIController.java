package edu.eci.arsw.blueprints.controllers;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/blueprints")
@Tag(name = "blueprintsApiController", description = "PARCIAL ARSW T2 laura venegas")
public class BlueprintsAPIController {

    private final BlueprintsServices services;

    public BlueprintsAPIController(BlueprintsServices services) { this.services = services; }

    // GET /blueprints
    @GetMapping
    @Operation(summary = "Obtener todos los blueprints", description = "Retorna una lista completa de blueprints")
    public ResponseEntity<ApiResponse<Set<Blueprint>>> getAll() {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "execute ok", services.getAllBlueprints()));
    }

    // GET /blueprints/{author}
    @GetMapping("/{author}")
    @Operation(summary = "Obtener los blueprints por autor", description = "Retorna una lista de los blueprints por autor")
    public ResponseEntity<ApiResponse<?>> byAuthor(@PathVariable String author) {
        try {
            return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "execute ok", services.getBlueprintsByAuthor(author)));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(HttpStatus.NOT_FOUND.value(), e.getMessage(), null));
        }
    }

    // GET /blueprints/{author}/{bpname}
    @GetMapping("/{author}/{bpname}")
    @Operation(summary = "Obtener los blueprints por autor y por nombre del blueprint", description = "Retorna el blueprint por nombre de un autor")
    public ResponseEntity<ApiResponse<?>> byAuthorAndName(@PathVariable String author, @PathVariable String bpname) {
        try {
            return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "execute ok", services.getBlueprint(author, bpname)));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(HttpStatus.NOT_FOUND.value(), e.getMessage(), null));
        }
    }

    // POST /blueprints
    @PostMapping
    @Operation(summary = "Registrar un nuevo blueprint", description = "Se crea y almacena un nuevo blueprint")
    public ResponseEntity<ApiResponse<?>> add(@Valid @RequestBody NewBlueprintRequest req) {
        try {
            Blueprint bp = new Blueprint(req.author(), req.name(), req.points());
            services.addNewBlueprint(bp);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(HttpStatus.CREATED.value(), "blueprint created", bp));
        } catch (BlueprintPersistenceException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(HttpStatus.FORBIDDEN.value(), e.getMessage(), null));
        }
    }

    // PUT /blueprints/{author}/{bpname}/points
    @PutMapping("/{author}/{bpname}/points")
    @Operation(summary = "Agregar punto a blueprint", description = "Agrega un nuevo punto a un blueprint existente")
    public ResponseEntity<ApiResponse<?>> addPoint(@PathVariable String author, @PathVariable String bpname,
                                                    @RequestBody Point p) {
        try {
            services.addPoint(author, bpname, p.x(), p.y());
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(new ApiResponse<>(HttpStatus.ACCEPTED.value(), "point added", p));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(HttpStatus.NOT_FOUND.value(), e.getMessage(), null));
        }
    }

    public record NewBlueprintRequest(
            @NotBlank String author,
            @NotBlank String name,
            @Valid java.util.List<Point> points
    ) { }
}
