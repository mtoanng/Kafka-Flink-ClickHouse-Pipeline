package vn.edu.ves.desktop.service;

import vn.edu.ves.desktop.dao.AlertRuleDao;
import vn.edu.ves.desktop.model.AlertRule;
import vn.edu.ves.desktop.model.MetricType;

import java.util.List;
import java.util.Optional;

public class AlertRuleServiceImpl implements AlertRuleService {

    private final AlertRuleDao dao;

    public AlertRuleServiceImpl() {
        this(new AlertRuleDao());
    }

    public AlertRuleServiceImpl(AlertRuleDao dao) {
        this.dao = dao;
    }

    @Override
    public List<AlertRule> findAll() { return dao.findAll(); }

    @Override
    public List<AlertRule> findByMetricType(MetricType metricType) {
        return dao.findByMetricType(metricType);
    }

    @Override
    public Optional<AlertRule> findById(long id) { return dao.findById(id); }

    @Override
    public AlertRule save(AlertRule rule) {
        if (rule == null) throw new IllegalArgumentException("rule null");
        if (rule.getRuleName() != null) {
            rule.setRuleName(rule.getRuleName().trim());
        }
        if (rule.getFuelType() != null) {
            rule.setFuelType(rule.getFuelType().trim().toUpperCase());
        }
        if (rule.getRegionCode() != null) {
            rule.setRegionCode(rule.getRegionCode().trim().toUpperCase());
        }
        return dao.save(rule);
    }

    @Override
    public boolean delete(long id) { return dao.delete(id); }

    @Override
    public boolean setEnabled(long id, boolean enabled) { return dao.setEnabled(id, enabled); }
}
