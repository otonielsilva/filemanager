package org.otoniel.resources.repo;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import org.otoniel.resources.models.FileData;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class FileRepository implements PanacheRepository<FileData> {

    public Optional<FileData> findById(String id) {
        return find("id", id).stream().findAny();
    }

    public List<FileData> findAll(Integer pageNumber, Integer pageSize) {
        final PanacheQuery<FileData> allQuery = this.findAll();
        return allQuery.page(pageNumber, pageSize).list();
    }
}