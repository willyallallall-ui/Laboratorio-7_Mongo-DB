/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.services;

import com.example.PersistenceManager;
import com.example.models.Producto;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
/**
 *
 * @author Willi
 */
@Path("/productos")
@Produces(MediaType.APPLICATION_JSON)
public class ProductoService {

    EntityManager entityManager;

    @PostConstruct
    public void init() {
        try {
            entityManager = PersistenceManager.getInstance().getEntityManagerFactory().createEntityManager();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // GET /productos -> todos los productos almacenados
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll() {
        Query q = entityManager.createQuery("select p from Producto p");
        List<Producto> productos = q.getResultList();
        return Response.status(200).header("Access-Control-Allow-Origin", "*").entity(productos).build();
    }
}