/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.services;

import com.example.PersistenceManager;
import com.example.models.Competitor;
import com.example.models.CompetitorDTO;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author Mauricio
 */
@Path("/competitors")
@Produces(MediaType.APPLICATION_JSON)
public class CompetitorService {

    @PersistenceContext(unitName = "mongoPU")
    EntityManager entityManager;

    @PostConstruct
    public void init() {
        try {
            entityManager = PersistenceManager.getInstance().getEntityManagerFactory().createEntityManager();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // GET /competitors -> todos los competidores ordenados por apellido
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll() {
        Query q = entityManager.createQuery("select u from Competitor u order by u.surname ASC");
        List<Competitor> competitors = q.getResultList();
        return Response.status(200).header("Access-Control-Allow-Origin", "*").entity(competitors).build();
    }

    // GET /competitors/startsWithA -> nombres de los competidores que empiezan por 'A'
    @GET
    @Path("/startsWithA")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNamesStartingWithA() {
        Query q = entityManager.createQuery("select u from Competitor u");
        List<Competitor> competitors = q.getResultList();

        JSONArray names = new JSONArray();
        for (Competitor c : competitors) {
            if (c.getName() != null && c.getName().toUpperCase().startsWith("A")) {
                names.add(c.getName());
            }
        }
        return Response.status(200).header("Access-Control-Allow-Origin", "*").entity(names.toJSONString()).build();
    }

    // GET /competitors/{name} -> competidores con ese nombre
    @GET
    @Path("{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCompetitorsByName(@PathParam("name") String name) {
        TypedQuery<Competitor> query = (TypedQuery<Competitor>) entityManager.createQuery("SELECT c FROM Competitor c WHERE c.name = :name");
        List<Competitor> competitors = query.setParameter("name", name).getResultList();
        return Response.status(200).header("Access-Control-Allow-Origin", "*").entity(competitors).build();
    }

    // POST /competitors/add -> crea un competidor con su vehículo y su producto (obligatorio)
    @POST
    @Path("/add")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createCompetitor(CompetitorDTO competitor) {

        JSONObject rta = new JSONObject();

        if (competitor.getProducto() == null) {
            rta.put("error", "El competidor debe tener un producto asociado");
            return Response.status(400).header("Access-Control-Allow-Origin", "*").entity(rta.toJSONString()).build();
        }

        Competitor c = new Competitor();
        c.setAddress(competitor.getAddress());
        c.setAge(competitor.getAge());
        c.setCellphone(competitor.getCellphone());
        c.setCity(competitor.getCity());
        c.setCountry(competitor.getCountry());
        c.setName(competitor.getName());
        c.setSurname(competitor.getSurname());
        c.setTelephone(competitor.getTelephone());
        c.setVehicle(competitor.getVehicle());
        c.setProducto(competitor.getProducto());

        int status = 200;
        try {
            entityManager.getTransaction().begin();
            entityManager.persist(c);
            entityManager.getTransaction().commit();
            entityManager.refresh(c);
            rta.put("competitor_id", c.getId());
        } catch (Throwable t) {
            t.printStackTrace();
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            rta.put("error", "No se pudo guardar el competidor");
            status = 500;
        } finally {
            entityManager.clear();
            entityManager.close();
        }
        return Response.status(status).header("Access-Control-Allow-Origin", "*").entity(rta.toJSONString()).build();
    }

    @OPTIONS
    public Response cors(@javax.ws.rs.core.Context HttpHeaders requestHeaders) {
        return Response.status(200).header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS").header("Access-Control-Allow-Headers", "AUTHORIZATION, content-type, accept").build();
    }
}