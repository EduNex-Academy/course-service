package org.edunex.courseservice.repository;

import org.edunex.courseservice.model.Module;
import org.edunex.courseservice.model.enums.ModuleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {
    List<Module> findByCourseId(Long courseId);
    List<Module> findByCourseIdOrderByModuleOrder(Long courseId);
    List<Module> findByType(ModuleType type);
    List<Module> findByCourseIdAndCoinsRequiredLessThanEqual(Long courseId, int coins);
}
