package com.webank.weevent.processor.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CEPRuleExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public CEPRuleExample() {
        oredCriteria = new ArrayList<Criteria>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<Criterion>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andIdIsNull() {
            addCriterion("id is null");
            return (Criteria) this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("id is not null");
            return (Criteria) this;
        }

        public Criteria andIdEqualTo(String value) {
            addCriterion("id =", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(String value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(String value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(String value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThan(String value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(String value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLike(String value) {
            addCriterion("id like", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotLike(String value) {
            addCriterion("id not like", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdIn(List<String> values) {
            addCriterion("id in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<String> values) {
            addCriterion("id not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(String value1, String value2) {
            addCriterion("id between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(String value1, String value2) {
            addCriterion("id not between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andRuleNameIsNull() {
            addCriterion("rule_name is null");
            return (Criteria) this;
        }

        public Criteria andRuleNameIsNotNull() {
            addCriterion("rule_name is not null");
            return (Criteria) this;
        }

        public Criteria andRuleNameEqualTo(String value) {
            addCriterion("rule_name =", value, "ruleName");
            return (Criteria) this;
        }

        public Criteria andRuleNameNotEqualTo(String value) {
            addCriterion("rule_name <>", value, "ruleName");
            return (Criteria) this;
        }

        public Criteria andRuleNameGreaterThan(String value) {
            addCriterion("rule_name >", value, "ruleName");
            return (Criteria) this;
        }

        public Criteria andRuleNameGreaterThanOrEqualTo(String value) {
            addCriterion("rule_name >=", value, "ruleName");
            return (Criteria) this;
        }

        public Criteria andRuleNameLessThan(String value) {
            addCriterion("rule_name <", value, "ruleName");
            return (Criteria) this;
        }

        public Criteria andRuleNameLessThanOrEqualTo(String value) {
            addCriterion("rule_name <=", value, "ruleName");
            return (Criteria) this;
        }

        public Criteria andRuleNameLike(String value) {
            addCriterion("rule_name like", value, "ruleName");
            return (Criteria) this;
        }

        public Criteria andRuleNameNotLike(String value) {
            addCriterion("rule_name not like", value, "ruleName");
            return (Criteria) this;
        }

        public Criteria andRuleNameIn(List<String> values) {
            addCriterion("rule_name in", values, "ruleName");
            return (Criteria) this;
        }

        public Criteria andRuleNameNotIn(List<String> values) {
            addCriterion("rule_name not in", values, "ruleName");
            return (Criteria) this;
        }

        public Criteria andRuleNameBetween(String value1, String value2) {
            addCriterion("rule_name between", value1, value2, "ruleName");
            return (Criteria) this;
        }

        public Criteria andRuleNameNotBetween(String value1, String value2) {
            addCriterion("rule_name not between", value1, value2, "ruleName");
            return (Criteria) this;
        }

        public Criteria andFromDestinationIsNull() {
            addCriterion("from_destination is null");
            return (Criteria) this;
        }

        public Criteria andFromDestinationIsNotNull() {
            addCriterion("from_destination is not null");
            return (Criteria) this;
        }

        public Criteria andFromDestinationEqualTo(String value) {
            addCriterion("from_destination =", value, "fromDestination");
            return (Criteria) this;
        }

        public Criteria andFromDestinationNotEqualTo(String value) {
            addCriterion("from_destination <>", value, "fromDestination");
            return (Criteria) this;
        }

        public Criteria andFromDestinationGreaterThan(String value) {
            addCriterion("from_destination >", value, "fromDestination");
            return (Criteria) this;
        }

        public Criteria andFromDestinationGreaterThanOrEqualTo(String value) {
            addCriterion("from_destination >=", value, "fromDestination");
            return (Criteria) this;
        }

        public Criteria andFromDestinationLessThan(String value) {
            addCriterion("from_destination <", value, "fromDestination");
            return (Criteria) this;
        }

        public Criteria andFromDestinationLessThanOrEqualTo(String value) {
            addCriterion("from_destination <=", value, "fromDestination");
            return (Criteria) this;
        }

        public Criteria andFromDestinationLike(String value) {
            addCriterion("from_destination like", value, "fromDestination");
            return (Criteria) this;
        }

        public Criteria andFromDestinationNotLike(String value) {
            addCriterion("from_destination not like", value, "fromDestination");
            return (Criteria) this;
        }

        public Criteria andFromDestinationIn(List<String> values) {
            addCriterion("from_destination in", values, "fromDestination");
            return (Criteria) this;
        }

        public Criteria andFromDestinationNotIn(List<String> values) {
            addCriterion("from_destination not in", values, "fromDestination");
            return (Criteria) this;
        }

        public Criteria andFromDestinationBetween(String value1, String value2) {
            addCriterion("from_destination between", value1, value2, "fromDestination");
            return (Criteria) this;
        }

        public Criteria andFromDestinationNotBetween(String value1, String value2) {
            addCriterion("from_destination not between", value1, value2, "fromDestination");
            return (Criteria) this;
        }

        public Criteria andBrokerUrlIsNull() {
            addCriterion("broker_url is null");
            return (Criteria) this;
        }

        public Criteria andBrokerUrlIsNotNull() {
            addCriterion("broker_url is not null");
            return (Criteria) this;
        }

        public Criteria andBrokerUrlEqualTo(String value) {
            addCriterion("broker_url =", value, "brokerUrl");
            return (Criteria) this;
        }

        public Criteria andBrokerUrlNotEqualTo(String value) {
            addCriterion("broker_url <>", value, "brokerUrl");
            return (Criteria) this;
        }

        public Criteria andBrokerUrlGreaterThan(String value) {
            addCriterion("broker_url >", value, "brokerUrl");
            return (Criteria) this;
        }

        public Criteria andBrokerUrlGreaterThanOrEqualTo(String value) {
            addCriterion("broker_url >=", value, "brokerUrl");
            return (Criteria) this;
        }

        public Criteria andBrokerUrlLessThan(String value) {
            addCriterion("broker_url <", value, "brokerUrl");
            return (Criteria) this;
        }

        public Criteria andBrokerUrlLessThanOrEqualTo(String value) {
            addCriterion("broker_url <=", value, "brokerUrl");
            return (Criteria) this;
        }

        public Criteria andBrokerUrlLike(String value) {
            addCriterion("broker_url like", value, "brokerUrl");
            return (Criteria) this;
        }

        public Criteria andBrokerUrlNotLike(String value) {
            addCriterion("broker_url not like", value, "brokerUrl");
            return (Criteria) this;
        }

        public Criteria andBrokerUrlIn(List<String> values) {
            addCriterion("broker_url in", values, "brokerUrl");
            return (Criteria) this;
        }

        public Criteria andBrokerUrlNotIn(List<String> values) {
            addCriterion("broker_url not in", values, "brokerUrl");
            return (Criteria) this;
        }

        public Criteria andBrokerUrlBetween(String value1, String value2) {
            addCriterion("broker_url between", value1, value2, "brokerUrl");
            return (Criteria) this;
        }

        public Criteria andBrokerUrlNotBetween(String value1, String value2) {
            addCriterion("broker_url not between", value1, value2, "brokerUrl");
            return (Criteria) this;
        }

        public Criteria andPayloadIsNull() {
            addCriterion("payload is null");
            return (Criteria) this;
        }

        public Criteria andPayloadIsNotNull() {
            addCriterion("payload is not null");
            return (Criteria) this;
        }

        public Criteria andPayloadEqualTo(String value) {
            addCriterion("payload =", value, "payload");
            return (Criteria) this;
        }

        public Criteria andPayloadNotEqualTo(String value) {
            addCriterion("payload <>", value, "payload");
            return (Criteria) this;
        }

        public Criteria andPayloadGreaterThan(String value) {
            addCriterion("payload >", value, "payload");
            return (Criteria) this;
        }

        public Criteria andPayloadGreaterThanOrEqualTo(String value) {
            addCriterion("payload >=", value, "payload");
            return (Criteria) this;
        }

        public Criteria andPayloadLessThan(String value) {
            addCriterion("payload <", value, "payload");
            return (Criteria) this;
        }

        public Criteria andPayloadLessThanOrEqualTo(String value) {
            addCriterion("payload <=", value, "payload");
            return (Criteria) this;
        }

        public Criteria andPayloadLike(String value) {
            addCriterion("payload like", value, "payload");
            return (Criteria) this;
        }

        public Criteria andPayloadNotLike(String value) {
            addCriterion("payload not like", value, "payload");
            return (Criteria) this;
        }

        public Criteria andPayloadIn(List<String> values) {
            addCriterion("payload in", values, "payload");
            return (Criteria) this;
        }

        public Criteria andPayloadNotIn(List<String> values) {
            addCriterion("payload not in", values, "payload");
            return (Criteria) this;
        }

        public Criteria andPayloadBetween(String value1, String value2) {
            addCriterion("payload between", value1, value2, "payload");
            return (Criteria) this;
        }

        public Criteria andPayloadNotBetween(String value1, String value2) {
            addCriterion("payload not between", value1, value2, "payload");
            return (Criteria) this;
        }

        public Criteria andPayloadTypeIsNull() {
            addCriterion("payload_type is null");
            return (Criteria) this;
        }

        public Criteria andPayloadTypeIsNotNull() {
            addCriterion("payload_type is not null");
            return (Criteria) this;
        }

        public Criteria andPayloadTypeEqualTo(Integer value) {
            addCriterion("payload_type =", value, "payloadType");
            return (Criteria) this;
        }

        public Criteria andPayloadTypeNotEqualTo(Integer value) {
            addCriterion("payload_type <>", value, "payloadType");
            return (Criteria) this;
        }

        public Criteria andPayloadTypeGreaterThan(Integer value) {
            addCriterion("payload_type >", value, "payloadType");
            return (Criteria) this;
        }

        public Criteria andPayloadTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("payload_type >=", value, "payloadType");
            return (Criteria) this;
        }

        public Criteria andPayloadTypeLessThan(Integer value) {
            addCriterion("payload_type <", value, "payloadType");
            return (Criteria) this;
        }

        public Criteria andPayloadTypeLessThanOrEqualTo(Integer value) {
            addCriterion("payload_type <=", value, "payloadType");
            return (Criteria) this;
        }

        public Criteria andPayloadTypeIn(List<Integer> values) {
            addCriterion("payload_type in", values, "payloadType");
            return (Criteria) this;
        }

        public Criteria andPayloadTypeNotIn(List<Integer> values) {
            addCriterion("payload_type not in", values, "payloadType");
            return (Criteria) this;
        }

        public Criteria andPayloadTypeBetween(Integer value1, Integer value2) {
            addCriterion("payload_type between", value1, value2, "payloadType");
            return (Criteria) this;
        }

        public Criteria andPayloadTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("payload_type not between", value1, value2, "payloadType");
            return (Criteria) this;
        }

        public Criteria andSelectFieldIsNull() {
            addCriterion("select_field is null");
            return (Criteria) this;
        }

        public Criteria andSelectFieldIsNotNull() {
            addCriterion("select_field is not null");
            return (Criteria) this;
        }

        public Criteria andSelectFieldEqualTo(String value) {
            addCriterion("select_field =", value, "selectField");
            return (Criteria) this;
        }

        public Criteria andSelectFieldNotEqualTo(String value) {
            addCriterion("select_field <>", value, "selectField");
            return (Criteria) this;
        }

        public Criteria andSelectFieldGreaterThan(String value) {
            addCriterion("select_field >", value, "selectField");
            return (Criteria) this;
        }

        public Criteria andSelectFieldGreaterThanOrEqualTo(String value) {
            addCriterion("select_field >=", value, "selectField");
            return (Criteria) this;
        }

        public Criteria andSelectFieldLessThan(String value) {
            addCriterion("select_field <", value, "selectField");
            return (Criteria) this;
        }

        public Criteria andSelectFieldLessThanOrEqualTo(String value) {
            addCriterion("select_field <=", value, "selectField");
            return (Criteria) this;
        }

        public Criteria andSelectFieldLike(String value) {
            addCriterion("select_field like", value, "selectField");
            return (Criteria) this;
        }

        public Criteria andSelectFieldNotLike(String value) {
            addCriterion("select_field not like", value, "selectField");
            return (Criteria) this;
        }

        public Criteria andSelectFieldIn(List<String> values) {
            addCriterion("select_field in", values, "selectField");
            return (Criteria) this;
        }

        public Criteria andSelectFieldNotIn(List<String> values) {
            addCriterion("select_field not in", values, "selectField");
            return (Criteria) this;
        }

        public Criteria andSelectFieldBetween(String value1, String value2) {
            addCriterion("select_field between", value1, value2, "selectField");
            return (Criteria) this;
        }

        public Criteria andSelectFieldNotBetween(String value1, String value2) {
            addCriterion("select_field not between", value1, value2, "selectField");
            return (Criteria) this;
        }

        public Criteria andConditionFieldIsNull() {
            addCriterion("condition_field is null");
            return (Criteria) this;
        }

        public Criteria andConditionFieldIsNotNull() {
            addCriterion("condition_field is not null");
            return (Criteria) this;
        }

        public Criteria andConditionFieldEqualTo(String value) {
            addCriterion("condition_field =", value, "conditionField");
            return (Criteria) this;
        }

        public Criteria andConditionFieldNotEqualTo(String value) {
            addCriterion("condition_field <>", value, "conditionField");
            return (Criteria) this;
        }

        public Criteria andConditionFieldGreaterThan(String value) {
            addCriterion("condition_field >", value, "conditionField");
            return (Criteria) this;
        }

        public Criteria andConditionFieldGreaterThanOrEqualTo(String value) {
            addCriterion("condition_field >=", value, "conditionField");
            return (Criteria) this;
        }

        public Criteria andConditionFieldLessThan(String value) {
            addCriterion("condition_field <", value, "conditionField");
            return (Criteria) this;
        }

        public Criteria andConditionFieldLessThanOrEqualTo(String value) {
            addCriterion("condition_field <=", value, "conditionField");
            return (Criteria) this;
        }

        public Criteria andConditionFieldLike(String value) {
            addCriterion("condition_field like", value, "conditionField");
            return (Criteria) this;
        }

        public Criteria andConditionFieldNotLike(String value) {
            addCriterion("condition_field not like", value, "conditionField");
            return (Criteria) this;
        }

        public Criteria andConditionFieldIn(List<String> values) {
            addCriterion("condition_field in", values, "conditionField");
            return (Criteria) this;
        }

        public Criteria andConditionFieldNotIn(List<String> values) {
            addCriterion("condition_field not in", values, "conditionField");
            return (Criteria) this;
        }

        public Criteria andConditionFieldBetween(String value1, String value2) {
            addCriterion("condition_field between", value1, value2, "conditionField");
            return (Criteria) this;
        }

        public Criteria andConditionFieldNotBetween(String value1, String value2) {
            addCriterion("condition_field not between", value1, value2, "conditionField");
            return (Criteria) this;
        }

        public Criteria andConditionTypeIsNull() {
            addCriterion("condition_type is null");
            return (Criteria) this;
        }

        public Criteria andConditionTypeIsNotNull() {
            addCriterion("condition_type is not null");
            return (Criteria) this;
        }

        public Criteria andConditionTypeEqualTo(Integer value) {
            addCriterion("condition_type =", value, "conditionType");
            return (Criteria) this;
        }

        public Criteria andConditionTypeNotEqualTo(Integer value) {
            addCriterion("condition_type <>", value, "conditionType");
            return (Criteria) this;
        }

        public Criteria andConditionTypeGreaterThan(Integer value) {
            addCriterion("condition_type >", value, "conditionType");
            return (Criteria) this;
        }

        public Criteria andConditionTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("condition_type >=", value, "conditionType");
            return (Criteria) this;
        }

        public Criteria andConditionTypeLessThan(Integer value) {
            addCriterion("condition_type <", value, "conditionType");
            return (Criteria) this;
        }

        public Criteria andConditionTypeLessThanOrEqualTo(Integer value) {
            addCriterion("condition_type <=", value, "conditionType");
            return (Criteria) this;
        }

        public Criteria andConditionTypeIn(List<Integer> values) {
            addCriterion("condition_type in", values, "conditionType");
            return (Criteria) this;
        }

        public Criteria andConditionTypeNotIn(List<Integer> values) {
            addCriterion("condition_type not in", values, "conditionType");
            return (Criteria) this;
        }

        public Criteria andConditionTypeBetween(Integer value1, Integer value2) {
            addCriterion("condition_type between", value1, value2, "conditionType");
            return (Criteria) this;
        }

        public Criteria andConditionTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("condition_type not between", value1, value2, "conditionType");
            return (Criteria) this;
        }

        public Criteria andToDestinationIsNull() {
            addCriterion("to_destination is null");
            return (Criteria) this;
        }

        public Criteria andToDestinationIsNotNull() {
            addCriterion("to_destination is not null");
            return (Criteria) this;
        }

        public Criteria andToDestinationEqualTo(String value) {
            addCriterion("to_destination =", value, "toDestination");
            return (Criteria) this;
        }

        public Criteria andToDestinationNotEqualTo(String value) {
            addCriterion("to_destination <>", value, "toDestination");
            return (Criteria) this;
        }

        public Criteria andToDestinationGreaterThan(String value) {
            addCriterion("to_destination >", value, "toDestination");
            return (Criteria) this;
        }

        public Criteria andToDestinationGreaterThanOrEqualTo(String value) {
            addCriterion("to_destination >=", value, "toDestination");
            return (Criteria) this;
        }

        public Criteria andToDestinationLessThan(String value) {
            addCriterion("to_destination <", value, "toDestination");
            return (Criteria) this;
        }

        public Criteria andToDestinationLessThanOrEqualTo(String value) {
            addCriterion("to_destination <=", value, "toDestination");
            return (Criteria) this;
        }

        public Criteria andToDestinationLike(String value) {
            addCriterion("to_destination like", value, "toDestination");
            return (Criteria) this;
        }

        public Criteria andToDestinationNotLike(String value) {
            addCriterion("to_destination not like", value, "toDestination");
            return (Criteria) this;
        }

        public Criteria andToDestinationIn(List<String> values) {
            addCriterion("to_destination in", values, "toDestination");
            return (Criteria) this;
        }

        public Criteria andToDestinationNotIn(List<String> values) {
            addCriterion("to_destination not in", values, "toDestination");
            return (Criteria) this;
        }

        public Criteria andToDestinationBetween(String value1, String value2) {
            addCriterion("to_destination between", value1, value2, "toDestination");
            return (Criteria) this;
        }

        public Criteria andToDestinationNotBetween(String value1, String value2) {
            addCriterion("to_destination not between", value1, value2, "toDestination");
            return (Criteria) this;
        }

        public Criteria andDatabaseUrlIsNull() {
            addCriterion("database_url is null");
            return (Criteria) this;
        }

        public Criteria andDatabaseUrlIsNotNull() {
            addCriterion("database_url is not null");
            return (Criteria) this;
        }

        public Criteria andDatabaseUrlEqualTo(String value) {
            addCriterion("database_url =", value, "databaseUrl");
            return (Criteria) this;
        }

        public Criteria andDatabaseUrlNotEqualTo(String value) {
            addCriterion("database_url <>", value, "databaseUrl");
            return (Criteria) this;
        }

        public Criteria andDatabaseUrlGreaterThan(String value) {
            addCriterion("database_url >", value, "databaseUrl");
            return (Criteria) this;
        }

        public Criteria andDatabaseUrlGreaterThanOrEqualTo(String value) {
            addCriterion("database_url >=", value, "databaseUrl");
            return (Criteria) this;
        }

        public Criteria andDatabaseUrlLessThan(String value) {
            addCriterion("database_url <", value, "databaseUrl");
            return (Criteria) this;
        }

        public Criteria andDatabaseUrlLessThanOrEqualTo(String value) {
            addCriterion("database_url <=", value, "databaseUrl");
            return (Criteria) this;
        }

        public Criteria andDatabaseUrlLike(String value) {
            addCriterion("database_url like", value, "databaseUrl");
            return (Criteria) this;
        }

        public Criteria andDatabaseUrlNotLike(String value) {
            addCriterion("database_url not like", value, "databaseUrl");
            return (Criteria) this;
        }

        public Criteria andDatabaseUrlIn(List<String> values) {
            addCriterion("database_url in", values, "databaseUrl");
            return (Criteria) this;
        }

        public Criteria andDatabaseUrlNotIn(List<String> values) {
            addCriterion("database_url not in", values, "databaseUrl");
            return (Criteria) this;
        }

        public Criteria andDatabaseUrlBetween(String value1, String value2) {
            addCriterion("database_url between", value1, value2, "databaseUrl");
            return (Criteria) this;
        }

        public Criteria andDatabaseUrlNotBetween(String value1, String value2) {
            addCriterion("database_url not between", value1, value2, "databaseUrl");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeIsNull() {
            addCriterion("created_time is null");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeIsNotNull() {
            addCriterion("created_time is not null");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeEqualTo(Date value) {
            addCriterion("created_time =", value, "createdTime");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeNotEqualTo(Date value) {
            addCriterion("created_time <>", value, "createdTime");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeGreaterThan(Date value) {
            addCriterion("created_time >", value, "createdTime");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("created_time >=", value, "createdTime");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeLessThan(Date value) {
            addCriterion("created_time <", value, "createdTime");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeLessThanOrEqualTo(Date value) {
            addCriterion("created_time <=", value, "createdTime");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeIn(List<Date> values) {
            addCriterion("created_time in", values, "createdTime");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeNotIn(List<Date> values) {
            addCriterion("created_time not in", values, "createdTime");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeBetween(Date value1, Date value2) {
            addCriterion("created_time between", value1, value2, "createdTime");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeNotBetween(Date value1, Date value2) {
            addCriterion("created_time not between", value1, value2, "createdTime");
            return (Criteria) this;
        }

        public Criteria andStatusIsNull() {
            addCriterion("status is null");
            return (Criteria) this;
        }

        public Criteria andStatusIsNotNull() {
            addCriterion("status is not null");
            return (Criteria) this;
        }

        public Criteria andStatusEqualTo(Integer value) {
            addCriterion("status =", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualTo(Integer value) {
            addCriterion("status <>", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThan(Integer value) {
            addCriterion("status >", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("status >=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThan(Integer value) {
            addCriterion("status <", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualTo(Integer value) {
            addCriterion("status <=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusIn(List<Integer> values) {
            addCriterion("status in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotIn(List<Integer> values) {
            addCriterion("status not in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusBetween(Integer value1, Integer value2) {
            addCriterion("status between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("status not between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andErrorDestinationIsNull() {
            addCriterion("error_destination is null");
            return (Criteria) this;
        }

        public Criteria andErrorDestinationIsNotNull() {
            addCriterion("error_destination is not null");
            return (Criteria) this;
        }

        public Criteria andErrorDestinationEqualTo(String value) {
            addCriterion("error_destination =", value, "errorDestination");
            return (Criteria) this;
        }

        public Criteria andErrorDestinationNotEqualTo(String value) {
            addCriterion("error_destination <>", value, "errorDestination");
            return (Criteria) this;
        }

        public Criteria andErrorDestinationGreaterThan(String value) {
            addCriterion("error_destination >", value, "errorDestination");
            return (Criteria) this;
        }

        public Criteria andErrorDestinationGreaterThanOrEqualTo(String value) {
            addCriterion("error_destination >=", value, "errorDestination");
            return (Criteria) this;
        }

        public Criteria andErrorDestinationLessThan(String value) {
            addCriterion("error_destination <", value, "errorDestination");
            return (Criteria) this;
        }

        public Criteria andErrorDestinationLessThanOrEqualTo(String value) {
            addCriterion("error_destination <=", value, "errorDestination");
            return (Criteria) this;
        }

        public Criteria andErrorDestinationLike(String value) {
            addCriterion("error_destination like", value, "errorDestination");
            return (Criteria) this;
        }

        public Criteria andErrorDestinationNotLike(String value) {
            addCriterion("error_destination not like", value, "errorDestination");
            return (Criteria) this;
        }

        public Criteria andErrorDestinationIn(List<String> values) {
            addCriterion("error_destination in", values, "errorDestination");
            return (Criteria) this;
        }

        public Criteria andErrorDestinationNotIn(List<String> values) {
            addCriterion("error_destination not in", values, "errorDestination");
            return (Criteria) this;
        }

        public Criteria andErrorDestinationBetween(String value1, String value2) {
            addCriterion("error_destination between", value1, value2, "errorDestination");
            return (Criteria) this;
        }

        public Criteria andErrorDestinationNotBetween(String value1, String value2) {
            addCriterion("error_destination not between", value1, value2, "errorDestination");
            return (Criteria) this;
        }

        public Criteria andErrorCodeIsNull() {
            addCriterion("error_code is null");
            return (Criteria) this;
        }

        public Criteria andErrorCodeIsNotNull() {
            addCriterion("error_code is not null");
            return (Criteria) this;
        }

        public Criteria andErrorCodeEqualTo(String value) {
            addCriterion("error_code =", value, "errorCode");
            return (Criteria) this;
        }

        public Criteria andErrorCodeNotEqualTo(String value) {
            addCriterion("error_code <>", value, "errorCode");
            return (Criteria) this;
        }

        public Criteria andErrorCodeGreaterThan(String value) {
            addCriterion("error_code >", value, "errorCode");
            return (Criteria) this;
        }

        public Criteria andErrorCodeGreaterThanOrEqualTo(String value) {
            addCriterion("error_code >=", value, "errorCode");
            return (Criteria) this;
        }

        public Criteria andErrorCodeLessThan(String value) {
            addCriterion("error_code <", value, "errorCode");
            return (Criteria) this;
        }

        public Criteria andErrorCodeLessThanOrEqualTo(String value) {
            addCriterion("error_code <=", value, "errorCode");
            return (Criteria) this;
        }

        public Criteria andErrorCodeLike(String value) {
            addCriterion("error_code like", value, "errorCode");
            return (Criteria) this;
        }

        public Criteria andErrorCodeNotLike(String value) {
            addCriterion("error_code not like", value, "errorCode");
            return (Criteria) this;
        }

        public Criteria andErrorCodeIn(List<String> values) {
            addCriterion("error_code in", values, "errorCode");
            return (Criteria) this;
        }

        public Criteria andErrorCodeNotIn(List<String> values) {
            addCriterion("error_code not in", values, "errorCode");
            return (Criteria) this;
        }

        public Criteria andErrorCodeBetween(String value1, String value2) {
            addCriterion("error_code between", value1, value2, "errorCode");
            return (Criteria) this;
        }

        public Criteria andErrorCodeNotBetween(String value1, String value2) {
            addCriterion("error_code not between", value1, value2, "errorCode");
            return (Criteria) this;
        }

        public Criteria andErrorMessageIsNull() {
            addCriterion("error_message is null");
            return (Criteria) this;
        }

        public Criteria andErrorMessageIsNotNull() {
            addCriterion("error_message is not null");
            return (Criteria) this;
        }

        public Criteria andErrorMessageEqualTo(String value) {
            addCriterion("error_message =", value, "errorMessage");
            return (Criteria) this;
        }

        public Criteria andErrorMessageNotEqualTo(String value) {
            addCriterion("error_message <>", value, "errorMessage");
            return (Criteria) this;
        }

        public Criteria andErrorMessageGreaterThan(String value) {
            addCriterion("error_message >", value, "errorMessage");
            return (Criteria) this;
        }

        public Criteria andErrorMessageGreaterThanOrEqualTo(String value) {
            addCriterion("error_message >=", value, "errorMessage");
            return (Criteria) this;
        }

        public Criteria andErrorMessageLessThan(String value) {
            addCriterion("error_message <", value, "errorMessage");
            return (Criteria) this;
        }

        public Criteria andErrorMessageLessThanOrEqualTo(String value) {
            addCriterion("error_message <=", value, "errorMessage");
            return (Criteria) this;
        }

        public Criteria andErrorMessageLike(String value) {
            addCriterion("error_message like", value, "errorMessage");
            return (Criteria) this;
        }

        public Criteria andErrorMessageNotLike(String value) {
            addCriterion("error_message not like", value, "errorMessage");
            return (Criteria) this;
        }

        public Criteria andErrorMessageIn(List<String> values) {
            addCriterion("error_message in", values, "errorMessage");
            return (Criteria) this;
        }

        public Criteria andErrorMessageNotIn(List<String> values) {
            addCriterion("error_message not in", values, "errorMessage");
            return (Criteria) this;
        }

        public Criteria andErrorMessageBetween(String value1, String value2) {
            addCriterion("error_message between", value1, value2, "errorMessage");
            return (Criteria) this;
        }

        public Criteria andErrorMessageNotBetween(String value1, String value2) {
            addCriterion("error_message not between", value1, value2, "errorMessage");
            return (Criteria) this;
        }

        public Criteria andUpdatedTimeIsNull() {
            addCriterion("updated_time is null");
            return (Criteria) this;
        }

        public Criteria andUpdatedTimeIsNotNull() {
            addCriterion("updated_time is not null");
            return (Criteria) this;
        }

        public Criteria andUpdatedTimeEqualTo(Date value) {
            addCriterion("updated_time =", value, "updatedTime");
            return (Criteria) this;
        }

        public Criteria andUpdatedTimeNotEqualTo(Date value) {
            addCriterion("updated_time <>", value, "updatedTime");
            return (Criteria) this;
        }

        public Criteria andUpdatedTimeGreaterThan(Date value) {
            addCriterion("updated_time >", value, "updatedTime");
            return (Criteria) this;
        }

        public Criteria andUpdatedTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("updated_time >=", value, "updatedTime");
            return (Criteria) this;
        }

        public Criteria andUpdatedTimeLessThan(Date value) {
            addCriterion("updated_time <", value, "updatedTime");
            return (Criteria) this;
        }

        public Criteria andUpdatedTimeLessThanOrEqualTo(Date value) {
            addCriterion("updated_time <=", value, "updatedTime");
            return (Criteria) this;
        }

        public Criteria andUpdatedTimeIn(List<Date> values) {
            addCriterion("updated_time in", values, "updatedTime");
            return (Criteria) this;
        }

        public Criteria andUpdatedTimeNotIn(List<Date> values) {
            addCriterion("updated_time not in", values, "updatedTime");
            return (Criteria) this;
        }

        public Criteria andUpdatedTimeBetween(Date value1, Date value2) {
            addCriterion("updated_time between", value1, value2, "updatedTime");
            return (Criteria) this;
        }

        public Criteria andUpdatedTimeNotBetween(Date value1, Date value2) {
            addCriterion("updated_time not between", value1, value2, "updatedTime");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }
    }

    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}