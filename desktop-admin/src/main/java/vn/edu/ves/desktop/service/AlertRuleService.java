package vn.edu.ves.desktop.service;

import vn.edu.ves.desktop.model.AlertRule;
import vn.edu.ves.desktop.model.MetricType;

import java.util.List;
import java.util.Optional;

public interface AlertRuleService {

    List<AlertRule> findAll();

    List<AlertRule> findByMetricType(MetricType metricType);

    Optional<AlertRule> findById(long id);

    AlertRule save(AlertRule rule);

    boolean delete(long id);

    boolean setEnabled(long id, boolean enabled);
}
