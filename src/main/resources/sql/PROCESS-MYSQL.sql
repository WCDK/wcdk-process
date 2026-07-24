SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ACT_EVT_LOG
-- ----------------------------
DROP TABLE IF EXISTS `ACT_EVT_LOG`;
CREATE TABLE `ACT_EVT_LOG` (

  `LOG_NR_` BIGINT NOT NULL,
  `TYPE_` VARCHAR(64),
  `PROC_DEF_ID_` VARCHAR(64),
  `PROC_INST_ID_` VARCHAR(64),
  `EXECUTION_ID_` VARCHAR(64),
  `TASK_ID_` VARCHAR(64),
  `TIME_STAMP_` DATETIME(6) NOT NULL,
  `USER_ID_` VARCHAR(255),
  `DATA_` LONGBLOB,
  `LOCK_OWNER_` VARCHAR(255),
  `LOCK_TIME_` DATETIME(6),
  `IS_PROCESSED_` TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of ACT_EVT_LOG
-- ----------------------------

-- ----------------------------
-- Table structure for ACT_GE_BYTEARRAY
-- ----------------------------
DROP TABLE IF EXISTS `ACT_GE_BYTEARRAY`;
CREATE TABLE `ACT_GE_BYTEARRAY` (

  `ID_` VARCHAR(64) NOT NULL,
  `REV_` INT,
  `NAME_` VARCHAR(255),
  `DEPLOYMENT_ID_` VARCHAR(64),
  `BYTES_` LONGBLOB,
  `GENERATED_` TINYINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of ACT_GE_BYTEARRAY
-- ----------------------------

-- ----------------------------
-- Table structure for ACT_GE_PROPERTY
-- ----------------------------
DROP TABLE IF EXISTS `ACT_GE_PROPERTY`;
CREATE TABLE `ACT_GE_PROPERTY` (

  `NAME_` VARCHAR(64) NOT NULL,
  `VALUE_` VARCHAR(300),
  `REV_` INT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of ACT_GE_PROPERTY
-- ----------------------------
INSERT INTO `ACT_GE_PROPERTY` VALUES ('common.schema.version', '8.0.0.0', 1);
INSERT INTO `ACT_GE_PROPERTY` VALUES ('next.dbid', '1', 1);
INSERT INTO `ACT_GE_PROPERTY` VALUES ('schema.version', '8.0.0.0', 1);
INSERT INTO `ACT_GE_PROPERTY` VALUES ('schema.history', 'create(8.0.0.0)', 1);
INSERT INTO `ACT_GE_PROPERTY` VALUES ('eventregistry.schema.version', '8.0.0.0', 1);
INSERT INTO `ACT_GE_PROPERTY` VALUES ('cfg.execution-related-entities-count', 'true', 1);
INSERT INTO `ACT_GE_PROPERTY` VALUES ('cfg.task-related-entities-count', 'true', 1);

-- ----------------------------
-- Table structure for ACT_HI_ACTINST
-- ----------------------------
DROP TABLE IF EXISTS `ACT_HI_ACTINST`;
CREATE TABLE `ACT_HI_ACTINST` (

  `ID_` VARCHAR(64) NOT NULL,
  `REV_` INT DEFAULT 1,
  `PROC_DEF_ID_` VARCHAR(64) NOT NULL,
  `PROC_INST_ID_` VARCHAR(64) NOT NULL,
  `EXECUTION_ID_` VARCHAR(64) NOT NULL,
  `ACT_ID_` VARCHAR(255) NOT NULL,
  `TASK_ID_` VARCHAR(64),
  `CALL_PROC_INST_ID_` VARCHAR(64),
  `ACT_NAME_` VARCHAR(255),
  `ACT_TYPE_` VARCHAR(255) NOT NULL,
  `ASSIGNEE_` VARCHAR(255),
  `COMPLETED_BY_` VARCHAR(255),
  `START_TIME_` DATETIME(6) NOT NULL,
  `END_TIME_` DATETIME(6),
  `TRANSACTION_ORDER_` INT,
  `DURATION_` BIGINT,
  `DELETE_REASON_` TEXT,
  `TENANT_ID_` VARCHAR(255) DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of ACT_HI_ACTINST
-- ----------------------------
INSERT INTO `ACT_HI_ACTINST` VALUES ('4181c833-7f33-11f1-a7e2-00ffaabbccdd', 1, '测试流程001:1:1ef0f9f7-7f31-11f1-accd-00ffaabbccdd', '418152fd-7f33-11f1-a7e2-00ffaabbccdd', '4181c832-7f33-11f1-a7e2-00ffaabbccdd', '_2', NULL, NULL, 'StartEvent', 'startEvent', NULL, NULL, '2026-07-14 11:22:34.428000', '2026-07-14 11:22:34.440000', 1, 12, NULL, '');
INSERT INTO `ACT_HI_ACTINST` VALUES ('41839cf4-7f33-11f1-a7e2-00ffaabbccdd', 1, '测试流程001:1:1ef0f9f7-7f31-11f1-accd-00ffaabbccdd', '418152fd-7f33-11f1-a7e2-00ffaabbccdd', '4181c832-7f33-11f1-a7e2-00ffaabbccdd', '_9', NULL, NULL, NULL, 'sequenceFlow', NULL, NULL, '2026-07-14 11:22:34.440000', '2026-07-14 11:22:34.440000', 2, 0, NULL, '');
INSERT INTO `ACT_HI_ACTINST` VALUES ('41839cf5-7f33-11f1-a7e2-00ffaabbccdd', 2, '测试流程001:1:1ef0f9f7-7f31-11f1-accd-00ffaabbccdd', '418152fd-7f33-11f1-a7e2-00ffaabbccdd', '4181c832-7f33-11f1-a7e2-00ffaabbccdd', '_4', '418b6526-7f33-11f1-a7e2-00ffaabbccdd', NULL, 'UserTask', 'userTask', NULL, NULL, '2026-07-14 11:22:34.440000', '2026-07-14 11:22:54.570000', 3, 20130, NULL, '');
INSERT INTO `ACT_HI_ACTINST` VALUES ('4d835c29-7f33-11f1-a7e2-00ffaabbccdd', 1, '测试流程001:1:1ef0f9f7-7f31-11f1-accd-00ffaabbccdd', '418152fd-7f33-11f1-a7e2-00ffaabbccdd', '4181c832-7f33-11f1-a7e2-00ffaabbccdd', '_10', NULL, NULL, NULL, 'sequenceFlow', NULL, NULL, '2026-07-14 11:22:54.571000', '2026-07-14 11:22:54.571000', 1, 0, NULL, '');
INSERT INTO `ACT_HI_ACTINST` VALUES ('4d83aa4a-7f33-11f1-a7e2-00ffaabbccdd', 1, '测试流程001:1:1ef0f9f7-7f31-11f1-accd-00ffaabbccdd', '418152fd-7f33-11f1-a7e2-00ffaabbccdd', '4181c832-7f33-11f1-a7e2-00ffaabbccdd', '_3', NULL, NULL, 'EndEvent', 'endEvent', NULL, NULL, '2026-07-14 11:22:54.573000', '2026-07-14 11:22:54.580000', 2, 7, NULL, '');

-- ----------------------------
-- Table structure for ACT_HI_ATTACHMENT
-- ----------------------------
DROP TABLE IF EXISTS `ACT_HI_ATTACHMENT`;
CREATE TABLE `ACT_HI_ATTACHMENT` (

  `ID_` VARCHAR(64) NOT NULL,
  `REV_` INT,
  `USER_ID_` VARCHAR(255),
  `NAME_` VARCHAR(255),
  `DESCRIPTION_` TEXT,
  `TYPE_` VARCHAR(255),
  `TASK_ID_` VARCHAR(64),
  `PROC_INST_ID_` VARCHAR(64),
  `URL_` TEXT,
  `CONTENT_ID_` VARCHAR(64),
  `TIME_` DATETIME(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of ACT_HI_ATTACHMENT
-- ----------------------------

-- ----------------------------
-- Table structure for ACT_HI_COMMENT
-- ----------------------------
DROP TABLE IF EXISTS `ACT_HI_COMMENT`;
CREATE TABLE `ACT_HI_COMMENT` (

  `ID_` VARCHAR(64) NOT NULL,
  `TYPE_` VARCHAR(255),
  `TIME_` DATETIME(6) NOT NULL,
  `USER_ID_` VARCHAR(255),
  `TASK_ID_` VARCHAR(64),
  `PROC_INST_ID_` VARCHAR(64),
  `ACTION_` VARCHAR(255),
  `MESSAGE_` TEXT,
  `FULL_MSG_` LONGBLOB
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of ACT_HI_COMMENT
-- ----------------------------

-- ----------------------------
-- Table structure for ACT_HI_DETAIL
-- ----------------------------
DROP TABLE IF EXISTS `ACT_HI_DETAIL`;
CREATE TABLE `ACT_HI_DETAIL` (

  `ID_` VARCHAR(64) NOT NULL,
  `TYPE_` VARCHAR(255) NOT NULL,
  `PROC_INST_ID_` VARCHAR(64),
  `EXECUTION_ID_` VARCHAR(64),
  `TASK_ID_` VARCHAR(64),
  `ACT_INST_ID_` VARCHAR(64),
  `NAME_` VARCHAR(255) NOT NULL,
  `VAR_TYPE_` VARCHAR(64),
  `REV_` INT,
  `TIME_` DATETIME(6) NOT NULL,
  `BYTEARRAY_ID_` VARCHAR(64),
  `DOUBLE_` DECIMAL(38,10),
  `LONG_` BIGINT,
  `TEXT_` TEXT,
  `TEXT2_` TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of ACT_HI_DETAIL
-- ----------------------------

-- ----------------------------
-- Table structure for ACT_HI_ENTITYLINK
-- ----------------------------
DROP TABLE IF EXISTS `ACT_HI_ENTITYLINK`;
CREATE TABLE `ACT_HI_ENTITYLINK` (

  `ID_` VARCHAR(64) NOT NULL,
  `LINK_TYPE_` VARCHAR(255),
  `CREATE_TIME_` DATETIME(6),
  `SCOPE_ID_` VARCHAR(255),
  `SUB_SCOPE_ID_` VARCHAR(255),
  `SCOPE_TYPE_` VARCHAR(255),
  `SCOPE_DEFINITION_ID_` VARCHAR(255),
  `PARENT_ELEMENT_ID_` VARCHAR(255),
  `REF_SCOPE_ID_` VARCHAR(255),
  `REF_SCOPE_TYPE_` VARCHAR(255),
  `REF_SCOPE_DEFINITION_ID_` VARCHAR(255),
  `ROOT_SCOPE_ID_` VARCHAR(255),
  `ROOT_SCOPE_TYPE_` VARCHAR(255),
  `HIERARCHY_TYPE_` VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of ACT_HI_ENTITYLINK
-- ----------------------------

-- ----------------------------
-- Table structure for ACT_HI_IDENTITYLINK
-- ----------------------------
DROP TABLE IF EXISTS `ACT_HI_IDENTITYLINK`;
CREATE TABLE `ACT_HI_IDENTITYLINK` (

  `ID_` VARCHAR(64) NOT NULL,
  `GROUP_ID_` VARCHAR(255),
  `TYPE_` VARCHAR(255),
  `USER_ID_` VARCHAR(255),
  `TASK_ID_` VARCHAR(64),
  `CREATE_TIME_` DATETIME(6),
  `PROC_INST_ID_` VARCHAR(64),
  `SCOPE_ID_` VARCHAR(255),
  `SUB_SCOPE_ID_` VARCHAR(255),
  `SCOPE_TYPE_` VARCHAR(255),
  `SCOPE_DEFINITION_ID_` VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of ACT_HI_IDENTITYLINK
-- ----------------------------
INSERT INTO `ACT_HI_IDENTITYLINK` VALUES ('aebdd4ea-7f30-11f1-accd-00ffaabbccdd', '1', 'candidate', NULL, 'aebd86c9-7f30-11f1-accd-00ffaabbccdd', '2026-07-14 11:04:09.204000', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `ACT_HI_IDENTITYLINK` VALUES ('aebe230b-7f30-11f1-accd-00ffaabbccdd', NULL, 'candidate', '张三', 'aebd86c9-7f30-11f1-accd-00ffaabbccdd', '2026-07-14 11:04:09.205000', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `ACT_HI_IDENTITYLINK` VALUES ('aebe230c-7f30-11f1-accd-00ffaabbccdd', NULL, 'participant', '张三', NULL, '2026-07-14 11:04:09.205000', 'aeb633c0-7f30-11f1-accd-00ffaabbccdd', NULL, NULL, NULL, NULL);
INSERT INTO `ACT_HI_IDENTITYLINK` VALUES ('684a10e6-7f33-11f1-a7e2-00ffaabbccdd', '1', 'candidate', NULL, '684a10e5-7f33-11f1-a7e2-00ffaabbccdd', '2026-07-14 11:23:39.495000', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `ACT_HI_IDENTITYLINK` VALUES ('684a5f07-7f33-11f1-a7e2-00ffaabbccdd', NULL, 'candidate', '张三', '684a10e5-7f33-11f1-a7e2-00ffaabbccdd', '2026-07-14 11:23:39.496000', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `ACT_HI_IDENTITYLINK` VALUES ('684a5f08-7f33-11f1-a7e2-00ffaabbccdd', NULL, 'participant', '张三', NULL, '2026-07-14 11:23:39.496000', '6849c2bb-7f33-11f1-a7e2-00ffaabbccdd', NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for ACT_HI_PROCINST
-- ----------------------------
DROP TABLE IF EXISTS `ACT_HI_PROCINST`;
CREATE TABLE `ACT_HI_PROCINST` (

  `ID_` VARCHAR(64) NOT NULL,
  `REV_` INT DEFAULT 1,
  `PROC_INST_ID_` VARCHAR(64) NOT NULL,
  `BUSINESS_KEY_` VARCHAR(255),
  `PROC_DEF_ID_` VARCHAR(64) NOT NULL,
  `START_TIME_` DATETIME(6) NOT NULL,
  `END_TIME_` DATETIME(6),
  `DURATION_` BIGINT,
  `START_USER_ID_` VARCHAR(255),
  `START_ACT_ID_` VARCHAR(255),
  `END_ACT_ID_` VARCHAR(255),
  `SUPER_PROCESS_INSTANCE_ID_` VARCHAR(64),
  `DELETE_REASON_` TEXT,
  `TENANT_ID_` VARCHAR(255) DEFAULT '',
  `NAME_` VARCHAR(255),
  `CALLBACK_ID_` VARCHAR(255),
  `CALLBACK_TYPE_` VARCHAR(255),
  `REFERENCE_ID_` VARCHAR(255),
  `REFERENCE_TYPE_` VARCHAR(255),
  `PROPAGATED_STAGE_INST_ID_` VARCHAR(255),
  `BUSINESS_STATUS_` VARCHAR(255),
  `END_USER_ID_` VARCHAR(255),
  `STATE_` VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of ACT_HI_PROCINST
-- ----------------------------

-- ----------------------------
-- Table structure for ACT_HI_TASKINST
-- ----------------------------
DROP TABLE IF EXISTS `ACT_HI_TASKINST`;
CREATE TABLE `ACT_HI_TASKINST` (

  `ID_` VARCHAR(64) NOT NULL,
  `REV_` INT DEFAULT 1,
  `PROC_DEF_ID_` VARCHAR(64),
  `TASK_DEF_ID_` VARCHAR(64),
  `TASK_DEF_KEY_` VARCHAR(255),
  `PROC_INST_ID_` VARCHAR(64),
  `EXECUTION_ID_` VARCHAR(64),
  `SCOPE_ID_` VARCHAR(255),
  `SUB_SCOPE_ID_` VARCHAR(255),
  `SCOPE_TYPE_` VARCHAR(255),
  `SCOPE_DEFINITION_ID_` VARCHAR(255),
  `PROPAGATED_STAGE_INST_ID_` VARCHAR(255),
  `PARENT_TASK_ID_` VARCHAR(64),
  `STATE_` VARCHAR(255),
  `NAME_` VARCHAR(255),
  `DESCRIPTION_` TEXT,
  `OWNER_` VARCHAR(255),
  `ASSIGNEE_` VARCHAR(255),
  `START_TIME_` DATETIME(6) NOT NULL,
  `IN_PROGRESS_TIME_` DATETIME(6),
  `IN_PROGRESS_STARTED_BY_` VARCHAR(255),
  `CLAIM_TIME_` DATETIME(6),
  `CLAIMED_BY_` VARCHAR(255),
  `SUSPENDED_TIME_` DATETIME(6),
  `SUSPENDED_BY_` VARCHAR(255),
  `END_TIME_` DATETIME(6),
  `COMPLETED_BY_` VARCHAR(255),
  `DURATION_` BIGINT,
  `DELETE_REASON_` TEXT,
  `PRIORITY_` INT,
  `IN_PROGRESS_DUE_DATE_` DATETIME(6),
  `DUE_DATE_` DATETIME(6),
  `FORM_KEY_` VARCHAR(255),
  `CATEGORY_` VARCHAR(255),
  `TENANT_ID_` VARCHAR(255) DEFAULT '',
  `LAST_UPDATED_TIME_` DATETIME(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of ACT_HI_TASKINST
-- ----------------------------

-- ----------------------------
-- Table structure for ACT_HI_TSK_LOG
-- ----------------------------
DROP TABLE IF EXISTS `ACT_HI_TSK_LOG`;
CREATE TABLE `ACT_HI_TSK_LOG` (

  `ID_` BIGINT NOT NULL,
  `TYPE_` VARCHAR(64),
  `TASK_ID_` VARCHAR(64) NOT NULL,
  `TIME_STAMP_` DATETIME(6) NOT NULL,
  `USER_ID_` VARCHAR(255),
  `DATA_` TEXT,
  `EXECUTION_ID_` VARCHAR(64),
  `PROC_INST_ID_` VARCHAR(64),
  `PROC_DEF_ID_` VARCHAR(64),
  `SCOPE_ID_` VARCHAR(255),
  `SCOPE_DEFINITION_ID_` VARCHAR(255),
  `SUB_SCOPE_ID_` VARCHAR(255),
  `SCOPE_TYPE_` VARCHAR(255),
  `TENANT_ID_` VARCHAR(255) DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of ACT_HI_TSK_LOG
-- ----------------------------

-- ----------------------------
-- Table structure for ACT_HI_VARINST
-- ----------------------------
DROP TABLE IF EXISTS `ACT_HI_VARINST`;
CREATE TABLE `ACT_HI_VARINST` (

  `ID_` VARCHAR(64) NOT NULL,
  `REV_` INT DEFAULT 1,
  `PROC_INST_ID_` VARCHAR(64),
  `EXECUTION_ID_` VARCHAR(64),
  `TASK_ID_` VARCHAR(64),
  `NAME_` VARCHAR(255) NOT NULL,
  `VAR_TYPE_` VARCHAR(100),
  `SCOPE_ID_` VARCHAR(255),
  `SUB_SCOPE_ID_` VARCHAR(255),
  `SCOPE_TYPE_` VARCHAR(255),
  `BYTEARRAY_ID_` VARCHAR(64),
  `DOUBLE_` DECIMAL(38,10),
  `LONG_` BIGINT,
  `TEXT_` TEXT,
  `TEXT2_` TEXT,
  `META_INFO_` TEXT,
  `CREATE_TIME_` DATETIME(6),
  `LAST_UPDATED_TIME_` DATETIME(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of ACT_HI_VARINST
-- ----------------------------
INSERT INTO `ACT_HI_VARINST` VALUES ('aeb633c1-7f30-11f1-accd-00ffaabbccdd', 0, 'aeb633c0-7f30-11f1-accd-00ffaabbccdd', 'aeb633c0-7f30-11f1-accd-00ffaabbccdd', NULL, 'Property 1', 'string', NULL, NULL, NULL, NULL, NULL, NULL, 'Property value 1', NULL, NULL, '2026-07-14 11:04:09.153000', '2026-07-14 11:04:09.153000');
INSERT INTO `ACT_HI_VARINST` VALUES ('aeb6f712-7f30-11f1-accd-00ffaabbccdd', 0, 'aeb633c0-7f30-11f1-accd-00ffaabbccdd', 'aeb633c0-7f30-11f1-accd-00ffaabbccdd', NULL, 'starter', 'string', NULL, NULL, NULL, NULL, NULL, NULL, 'SYSTEM', NULL, NULL, '2026-07-14 11:04:09.158000', '2026-07-14 11:04:09.158000');
INSERT INTO `ACT_HI_VARINST` VALUES ('aeb6f713-7f30-11f1-accd-00ffaabbccdd', 0, 'aeb633c0-7f30-11f1-accd-00ffaabbccdd', 'aeb633c0-7f30-11f1-accd-00ffaabbccdd', NULL, 'businessTitle', 'string', NULL, NULL, NULL, NULL, NULL, NULL, '测试流程002', NULL, NULL, '2026-07-14 11:04:09.158000', '2026-07-14 11:04:09.158000');
INSERT INTO `ACT_HI_VARINST` VALUES ('aeb6f714-7f30-11f1-accd-00ffaabbccdd', 0, 'aeb633c0-7f30-11f1-accd-00ffaabbccdd', 'aeb633c0-7f30-11f1-accd-00ffaabbccdd', NULL, 'processNo', 'string', NULL, NULL, NULL, NULL, NULL, NULL, 'PROCESS-20260714110405010', NULL, NULL, '2026-07-14 11:04:09.158000', '2026-07-14 11:04:09.158000');
INSERT INTO `ACT_HI_VARINST` VALUES ('d500e7fd-7f30-11f1-accd-00ffaabbccdd', 0, 'aeb633c0-7f30-11f1-accd-00ffaabbccdd', 'aeb633c0-7f30-11f1-accd-00ffaabbccdd', NULL, 'approved', 'boolean', NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL, NULL, '2026-07-14 11:05:13.397000', '2026-07-14 11:05:13.397000');
INSERT INTO `ACT_HI_VARINST` VALUES ('d5010f0e-7f30-11f1-accd-00ffaabbccdd', 0, 'aeb633c0-7f30-11f1-accd-00ffaabbccdd', 'aeb633c0-7f30-11f1-accd-00ffaabbccdd', NULL, 'comment', 'string', NULL, NULL, NULL, NULL, NULL, NULL, '1', NULL, NULL, '2026-07-14 11:05:13.397000', '2026-07-14 11:05:13.397000');
INSERT INTO `ACT_HI_VARINST` VALUES ('4181a11e-7f33-11f1-a7e2-00ffaabbccdd', 0, '418152fd-7f33-11f1-a7e2-00ffaabbccdd', '418152fd-7f33-11f1-a7e2-00ffaabbccdd', NULL, 'starter', 'string', NULL, NULL, NULL, NULL, NULL, NULL, 'SYSTEM', NULL, NULL, '2026-07-14 11:22:34.428000', '2026-07-14 11:22:34.428000');
INSERT INTO `ACT_HI_VARINST` VALUES ('4181c82f-7f33-11f1-a7e2-00ffaabbccdd', 0, '418152fd-7f33-11f1-a7e2-00ffaabbccdd', '418152fd-7f33-11f1-a7e2-00ffaabbccdd', NULL, 'businessTitle', 'string', NULL, NULL, NULL, NULL, NULL, NULL, '测试流程001', NULL, NULL, '2026-07-14 11:22:34.428000', '2026-07-14 11:22:34.428000');
INSERT INTO `ACT_HI_VARINST` VALUES ('4181c830-7f33-11f1-a7e2-00ffaabbccdd', 0, '418152fd-7f33-11f1-a7e2-00ffaabbccdd', '418152fd-7f33-11f1-a7e2-00ffaabbccdd', NULL, 'taskName', 'string', NULL, NULL, NULL, NULL, NULL, NULL, 'asdasdada', NULL, NULL, '2026-07-14 11:22:34.428000', '2026-07-14 11:22:34.428000');
INSERT INTO `ACT_HI_VARINST` VALUES ('4181c831-7f33-11f1-a7e2-00ffaabbccdd', 0, '418152fd-7f33-11f1-a7e2-00ffaabbccdd', '418152fd-7f33-11f1-a7e2-00ffaabbccdd', NULL, 'processNo', 'string', NULL, NULL, NULL, NULL, NULL, NULL, 'PROCESS-20260714112226471', NULL, NULL, '2026-07-14 11:22:34.428000', '2026-07-14 11:22:34.428000');
INSERT INTO `ACT_HI_VARINST` VALUES ('4d7fb2a7-7f33-11f1-a7e2-00ffaabbccdd', 0, '418152fd-7f33-11f1-a7e2-00ffaabbccdd', '418152fd-7f33-11f1-a7e2-00ffaabbccdd', NULL, 'approved', 'boolean', NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL, NULL, '2026-07-14 11:22:54.547000', '2026-07-14 11:22:54.547000');
INSERT INTO `ACT_HI_VARINST` VALUES ('4d7fb2a8-7f33-11f1-a7e2-00ffaabbccdd', 0, '418152fd-7f33-11f1-a7e2-00ffaabbccdd', '418152fd-7f33-11f1-a7e2-00ffaabbccdd', NULL, 'comment', 'string', NULL, NULL, NULL, NULL, NULL, NULL, '', NULL, NULL, '2026-07-14 11:22:54.548000', '2026-07-14 11:22:54.548000');
INSERT INTO `ACT_HI_VARINST` VALUES ('6849c2bc-7f33-11f1-a7e2-00ffaabbccdd', 0, '6849c2bb-7f33-11f1-a7e2-00ffaabbccdd', '6849c2bb-7f33-11f1-a7e2-00ffaabbccdd', NULL, 'Property 1', 'string', NULL, NULL, NULL, NULL, NULL, NULL, 'Property value 1', NULL, NULL, '2026-07-14 11:23:39.492000', '2026-07-14 11:23:39.492000');
INSERT INTO `ACT_HI_VARINST` VALUES ('6849c2bd-7f33-11f1-a7e2-00ffaabbccdd', 0, '6849c2bb-7f33-11f1-a7e2-00ffaabbccdd', '6849c2bb-7f33-11f1-a7e2-00ffaabbccdd', NULL, 'taskName', 'string', NULL, NULL, NULL, NULL, NULL, NULL, 'adadasda', NULL, NULL, '2026-07-14 11:23:39.492000', '2026-07-14 11:23:39.492000');
INSERT INTO `ACT_HI_VARINST` VALUES ('6849c2be-7f33-11f1-a7e2-00ffaabbccdd', 0, '6849c2bb-7f33-11f1-a7e2-00ffaabbccdd', '6849c2bb-7f33-11f1-a7e2-00ffaabbccdd', NULL, 'starter', 'string', NULL, NULL, NULL, NULL, NULL, NULL, 'SYSTEM', NULL, NULL, '2026-07-14 11:23:39.492000', '2026-07-14 11:23:39.492000');
INSERT INTO `ACT_HI_VARINST` VALUES ('6849c2bf-7f33-11f1-a7e2-00ffaabbccdd', 0, '6849c2bb-7f33-11f1-a7e2-00ffaabbccdd', '6849c2bb-7f33-11f1-a7e2-00ffaabbccdd', NULL, 'businessTitle', 'string', NULL, NULL, NULL, NULL, NULL, NULL, '测试流程002', NULL, NULL, '2026-07-14 11:23:39.492000', '2026-07-14 11:23:39.492000');
INSERT INTO `ACT_HI_VARINST` VALUES ('6849c2c0-7f33-11f1-a7e2-00ffaabbccdd', 0, '6849c2bb-7f33-11f1-a7e2-00ffaabbccdd', '6849c2bb-7f33-11f1-a7e2-00ffaabbccdd', NULL, 'processNo', 'string', NULL, NULL, NULL, NULL, NULL, NULL, 'PROCESS-20260714112339482', NULL, NULL, '2026-07-14 11:23:39.492000', '2026-07-14 11:23:39.492000');
INSERT INTO `ACT_HI_VARINST` VALUES ('6c1d38f9-7f33-11f1-a7e2-00ffaabbccdd', 1, '6849c2bb-7f33-11f1-a7e2-00ffaabbccdd', '6849c2bb-7f33-11f1-a7e2-00ffaabbccdd', NULL, 'approved', 'boolean', NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL, NULL, '2026-07-14 11:23:45.911000', '2026-07-14 11:24:03.120000');
INSERT INTO `ACT_HI_VARINST` VALUES ('6c1d38fa-7f33-11f1-a7e2-00ffaabbccdd', 1, '6849c2bb-7f33-11f1-a7e2-00ffaabbccdd', '6849c2bb-7f33-11f1-a7e2-00ffaabbccdd', NULL, 'comment', 'string', NULL, NULL, NULL, NULL, NULL, NULL, '', NULL, NULL, '2026-07-14 11:23:45.912000', '2026-07-14 11:24:03.124000');

-- ----------------------------
-- Table structure for ACT_ID_BYTEARRAY
-- ----------------------------
DROP TABLE IF EXISTS `ACT_ID_BYTEARRAY`;
CREATE TABLE `ACT_ID_BYTEARRAY` (

  `ID_` VARCHAR(64) NOT NULL,
  `REV_` INT,
  `NAME_` VARCHAR(255),
  `BYTES_` LONGBLOB
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of ACT_ID_BYTEARRAY
-- ----------------------------

-- ----------------------------
-- Table structure for ACT_ID_GROUP
-- ----------------------------
DROP TABLE IF EXISTS `ACT_ID_GROUP`;
CREATE TABLE `ACT_ID_GROUP` (

  `ID_` VARCHAR(64) NOT NULL,
  `REV_` INT,
  `NAME_` VARCHAR(255),
  `TYPE_` VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of ACT_ID_GROUP
-- ----------------------------

-- ----------------------------
-- Table structure for ACT_ID_INFO
-- ----------------------------
DROP TABLE IF EXISTS `ACT_ID_INFO`;
CREATE TABLE `ACT_ID_INFO` (

  `ID_` VARCHAR(64) NOT NULL,
  `REV_` INT,
  `USER_ID_` VARCHAR(64),
  `TYPE_` VARCHAR(64),
  `KEY_` VARCHAR(255),
  `VALUE_` VARCHAR(255),
  `PASSWORD_` LONGBLOB,
  `PARENT_ID_` VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of ACT_ID_INFO
-- ----------------------------

-- ----------------------------
-- Table structure for ACT_ID_MEMBERSHIP
-- ----------------------------
DROP TABLE IF EXISTS `ACT_ID_MEMBERSHIP`;
CREATE TABLE `ACT_ID_MEMBERSHIP` (

  `USER_ID_` VARCHAR(64) NOT NULL,
  `GROUP_ID_` VARCHAR(64) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of ACT_ID_MEMBERSHIP
-- ----------------------------

-- ----------------------------
-- Table structure for ACT_ID_PRIV
-- ----------------------------
DROP TABLE IF EXISTS `ACT_ID_PRIV`;
CREATE TABLE `ACT_ID_PRIV` (

  `ID_` VARCHAR(64) NOT NULL,
  `NAME_` VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of ACT_ID_PRIV
-- ----------------------------

-- ----------------------------
-- Table structure for ACT_ID_PRIV_MAPPING
-- ----------------------------
DROP TABLE IF EXISTS `ACT_ID_PRIV_MAPPING`;
CREATE TABLE `ACT_ID_PRIV_MAPPING` (

  `ID_` VARCHAR(64) NOT NULL,
  `PRIV_ID_` VARCHAR(64) NOT NULL,
  `USER_ID_` VARCHAR(255),
  `GROUP_ID_` VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of ACT_ID_PRIV_MAPPING
-- ----------------------------

-- ----------------------------
-- Table structure for ACT_ID_PROPERTY
-- ----------------------------
DROP TABLE IF EXISTS `ACT_ID_PROPERTY`;
CREATE TABLE `ACT_ID_PROPERTY` (

  `NAME_` VARCHAR(64) NOT NULL,
  `VALUE_` VARCHAR(300),
  `REV_` INT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of ACT_ID_PROPERTY
-- ----------------------------
INSERT INTO `ACT_ID_PROPERTY` VALUES ('schema.version', '8.0.0.0', 1);

-- ----------------------------
-- Table structure for ACT_ID_TOKEN
-- ----------------------------
DROP TABLE IF EXISTS `ACT_ID_TOKEN`;
CREATE TABLE `ACT_ID_TOKEN` (

  `ID_` VARCHAR(64) NOT NULL,
  `REV_` INT,
  `TOKEN_VALUE_` VARCHAR(255),
  `TOKEN_DATE_` DATETIME(6),
  `IP_ADDRESS_` VARCHAR(255),
  `USER_AGENT_` VARCHAR(255),
  `USER_ID_` VARCHAR(255),
  `TOKEN_DATA_` TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of ACT_ID_TOKEN
-- ----------------------------

-- ----------------------------
-- Table structure for ACT_ID_USER
-- ----------------------------
DROP TABLE IF EXISTS `ACT_ID_USER`;
CREATE TABLE `ACT_ID_USER` (

  `ID_` VARCHAR(64) NOT NULL,
  `REV_` INT,
  `FIRST_` VARCHAR(255),
  `LAST_` VARCHAR(255),
  `DISPLAY_NAME_` VARCHAR(255),
  `EMAIL_` VARCHAR(255),
  `PWD_` VARCHAR(255),
  `PICTURE_ID_` VARCHAR(64),
  `TENANT_ID_` VARCHAR(255) DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of ACT_ID_USER
-- ----------------------------

-- ----------------------------
-- Table structure for ACT_PROCDEF_INFO
-- ----------------------------
DROP TABLE IF EXISTS `ACT_PROCDEF_INFO`;
CREATE TABLE `ACT_PROCDEF_INFO` (

  `ID_` VARCHAR(64) NOT NULL,
  `PROC_DEF_ID_` VARCHAR(64) NOT NULL,
  `REV_` `INT`,
  `INFO_JSON_ID_` VARCHAR(64)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of ACT_PROCDEF_INFO
-- ----------------------------

-- ----------------------------
-- Table structure for ACT_RE_DEPLOYMENT
-- ----------------------------
DROP TABLE IF EXISTS `ACT_RE_DEPLOYMENT`;
CREATE TABLE `ACT_RE_DEPLOYMENT` (

  `ID_` VARCHAR(64) NOT NULL,
  `NAME_` VARCHAR(255),
  `CATEGORY_` VARCHAR(255),
  `KEY_` VARCHAR(255),
  `TENANT_ID_` VARCHAR(255) DEFAULT '',
  `DEPLOY_TIME_` DATETIME(6),
  `DERIVED_FROM_` VARCHAR(64),
  `DERIVED_FROM_ROOT_` VARCHAR(64),
  `PARENT_DEPLOYMENT_ID_` VARCHAR(255),
  `ENGINE_VERSION_` VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of ACT_RE_DEPLOYMENT
-- ----------------------------

-- ----------------------------
-- Table structure for ACT_RE_MODEL
-- ----------------------------
DROP TABLE IF EXISTS `ACT_RE_MODEL`;
CREATE TABLE `ACT_RE_MODEL` (

  `ID_` VARCHAR(64) NOT NULL,
  `REV_` INT,
  `NAME_` VARCHAR(255),
  `KEY_` VARCHAR(255),
  `CATEGORY_` VARCHAR(255),
  `CREATE_TIME_` DATETIME(6),
  `LAST_UPDATE_TIME_` DATETIME(6),
  `VERSION_` INT,
  `META_INFO_` TEXT,
  `DEPLOYMENT_ID_` VARCHAR(64),
  `EDITOR_SOURCE_VALUE_ID_` VARCHAR(64),
  `EDITOR_SOURCE_EXTRA_VALUE_ID_` VARCHAR(64),
  `TENANT_ID_` VARCHAR(255) DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of ACT_RE_MODEL
-- ----------------------------

-- ----------------------------
-- Table structure for ACT_RE_PROCDEF
-- ----------------------------
DROP TABLE IF EXISTS `ACT_RE_PROCDEF`;
CREATE TABLE `ACT_RE_PROCDEF` (

  `ID_` VARCHAR(64) NOT NULL,
  `REV_` INT,
  `CATEGORY_` VARCHAR(255),
  `NAME_` VARCHAR(255),
  `KEY_` VARCHAR(255) NOT NULL,
  `VERSION_` INT NOT NULL,
  `DEPLOYMENT_ID_` VARCHAR(64),
  `RESOURCE_NAME_` TEXT,
  `DGRM_RESOURCE_NAME_` `varchar`,
  `DESCRIPTION_` TEXT,
  `HAS_START_FORM_KEY_` TINYINT,
  `HAS_GRAPHICAL_NOTATION_` TINYINT,
  `SUSPENSION_STATE_` INT,
  `TENANT_ID_` VARCHAR(255) DEFAULT '',
  `DERIVED_FROM_` VARCHAR(64),
  `DERIVED_FROM_ROOT_` VARCHAR(64),
  `DERIVED_VERSION_` INT DEFAULT 0 NOT NULL,
  `ENGINE_VERSION_` VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of ACT_RE_PROCDEF
-- ----------------------------

-- ----------------------------
-- Table structure for ACT_RU_ACTINST
-- ----------------------------
DROP TABLE IF EXISTS `ACT_RU_ACTINST`;
CREATE TABLE `ACT_RU_ACTINST` (

  `ID_` VARCHAR(64) NOT NULL,
  `REV_` INT DEFAULT 1,
  `PROC_DEF_ID_` VARCHAR(64) NOT NULL,
  `PROC_INST_ID_` VARCHAR(64) NOT NULL,
  `EXECUTION_ID_` VARCHAR(64) NOT NULL,
  `ACT_ID_` VARCHAR(255) NOT NULL,
  `TASK_ID_` VARCHAR(64),
  `CALL_PROC_INST_ID_` VARCHAR(64),
  `ACT_NAME_` VARCHAR(255),
  `ACT_TYPE_` VARCHAR(255) NOT NULL,
  `ASSIGNEE_` VARCHAR(255),
  `COMPLETED_BY_` VARCHAR(255),
  `START_TIME_` DATETIME(6) NOT NULL,
  `END_TIME_` DATETIME(6),
  `DURATION_` BIGINT,
  `TRANSACTION_ORDER_` INT,
  `DELETE_REASON_` TEXT,
  `TENANT_ID_` VARCHAR(255) DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of ACT_RU_ACTINST
-- ----------------------------
INSERT INTO `ACT_RU_ACTINST` VALUES ('aeb71e26-7f30-11f1-accd-00ffaabbccdd', 1, '测试流程002:1:3e278f7a-7f2a-11f1-b076-00ffaabbccdd', 'aeb633c0-7f30-11f1-accd-00ffaabbccdd', 'aeb6f715-7f30-11f1-accd-00ffaabbccdd', 'sid-3bf99749-cfcf-4f8c-9ea4-7fc51d8ecc79', NULL, NULL, '开始', 'startEvent', NULL, NULL, '2026-07-14 11:04:09.159000', '2026-07-14 11:04:09.163000', 4, 1, NULL, '');
INSERT INTO `ACT_RU_ACTINST` VALUES ('aeb856a7-7f30-11f1-accd-00ffaabbccdd', 1, '测试流程002:1:3e278f7a-7f2a-11f1-b076-00ffaabbccdd', 'aeb633c0-7f30-11f1-accd-00ffaabbccdd', 'aeb6f715-7f30-11f1-accd-00ffaabbccdd', 'sid-63f9964c-2425-4e32-b660-05a3d8182f37', NULL, NULL, NULL, 'sequenceFlow', NULL, NULL, '2026-07-14 11:04:09.167000', '2026-07-14 11:04:09.167000', 0, 2, NULL, '');
INSERT INTO `ACT_RU_ACTINST` VALUES ('aeb856a8-7f30-11f1-accd-00ffaabbccdd', 2, '测试流程002:1:3e278f7a-7f2a-11f1-b076-00ffaabbccdd', 'aeb633c0-7f30-11f1-accd-00ffaabbccdd', 'aeb6f715-7f30-11f1-accd-00ffaabbccdd', 'sid-c8f65916-b9f2-40f1-a0e8-c5772b0f21d2', 'aebd86c9-7f30-11f1-accd-00ffaabbccdd', NULL, '用户审批1', 'userTask', NULL, NULL, '2026-07-14 11:04:09.167000', '2026-07-14 11:05:13.413000', 64246, 3, NULL, '');
INSERT INTO `ACT_RU_ACTINST` VALUES ('d503800f-7f30-11f1-accd-00ffaabbccdd', 1, '测试流程002:1:3e278f7a-7f2a-11f1-b076-00ffaabbccdd', 'aeb633c0-7f30-11f1-accd-00ffaabbccdd', 'aeb6f715-7f30-11f1-accd-00ffaabbccdd', 'sid-f66fbe22-258e-4829-89d9-2dca3352983f', NULL, NULL, NULL, 'sequenceFlow', NULL, NULL, '2026-07-14 11:05:13.413000', '2026-07-14 11:05:13.413000', 0, 1, NULL, '');
INSERT INTO `ACT_RU_ACTINST` VALUES ('d5038010-7f30-11f1-accd-00ffaabbccdd', 1, '测试流程002:1:3e278f7a-7f2a-11f1-b076-00ffaabbccdd', 'aeb633c0-7f30-11f1-accd-00ffaabbccdd', 'aeb6f715-7f30-11f1-accd-00ffaabbccdd', 'sid-afd4ac87-6d13-4983-9fe2-801646e48b10', NULL, NULL, NULL, 'exclusiveGateway', NULL, NULL, '2026-07-14 11:05:13.413000', '2026-07-14 11:05:13.413000', 0, 2, NULL, '');
INSERT INTO `ACT_RU_ACTINST` VALUES ('d5038011-7f30-11f1-accd-00ffaabbccdd', 1, '测试流程002:1:3e278f7a-7f2a-11f1-b076-00ffaabbccdd', 'aeb633c0-7f30-11f1-accd-00ffaabbccdd', 'aeb6f715-7f30-11f1-accd-00ffaabbccdd', 'sid-244b02e5-b484-421b-a314-9441dbcece46', NULL, NULL, NULL, 'sequenceFlow', NULL, NULL, '2026-07-14 11:05:13.413000', '2026-07-14 11:05:13.413000', 0, 3, NULL, '');
INSERT INTO `ACT_RU_ACTINST` VALUES ('d5046a72-7f30-11f1-accd-00ffaabbccdd', 1, '测试流程002:1:3e278f7a-7f2a-11f1-b076-00ffaabbccdd', 'aeb633c0-7f30-11f1-accd-00ffaabbccdd', 'aeb6f715-7f30-11f1-accd-00ffaabbccdd', 'sid-d7728353-e887-4849-8ba3-5b1d5e830867', 'd5049183-7f30-11f1-accd-00ffaabbccdd', NULL, NULL, 'userTask', NULL, NULL, '2026-07-14 11:05:13.419000', NULL, NULL, 4, NULL, '');

-- ----------------------------
-- Table structure for ACT_RU_DEADLETTER_JOB
-- ----------------------------
DROP TABLE IF EXISTS `ACT_RU_DEADLETTER_JOB`;
CREATE TABLE `ACT_RU_DEADLETTER_JOB` (

  `ID_` VARCHAR(64) NOT NULL,
  `REV_` INT,
  `CATEGORY_` VARCHAR(255),
  `TYPE_` VARCHAR(255) NOT NULL,
  `EXCLUSIVE_` TINYINT,
  `EXECUTION_ID_` VARCHAR(64),
  `PROCESS_INSTANCE_ID_` VARCHAR(64),
  `PROC_DEF_ID_` VARCHAR(64),
  `ELEMENT_ID_` VARCHAR(255),
  `ELEMENT_NAME_` VARCHAR(255),
  `SCOPE_ID_` VARCHAR(255),
  `SUB_SCOPE_ID_` VARCHAR(255),
  `SCOPE_TYPE_` VARCHAR(255),
  `SCOPE_DEFINITION_ID_` VARCHAR(255),
  `CORRELATION_ID_` VARCHAR(255),
  `EXCEPTION_STACK_ID_` VARCHAR(64),
  `EXCEPTION_MSG_` TEXT,
  `DUEDATE_` DATETIME(6),
  `REPEAT_` VARCHAR(255),
  `HANDLER_TYPE_` VARCHAR(255),
  `HANDLER_CFG_` TEXT,
  `CUSTOM_VALUES_ID_` VARCHAR(64),
  `CREATE_TIME_` DATETIME(6),
  `TENANT_ID_` VARCHAR(255) DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of ACT_RU_DEADLETTER_JOB
-- ----------------------------

-- ----------------------------
-- Table structure for ACT_RU_ENTITYLINK
-- ----------------------------
DROP TABLE IF EXISTS `ACT_RU_ENTITYLINK`;
CREATE TABLE `ACT_RU_ENTITYLINK` (

  `ID_` VARCHAR(64) NOT NULL,
  `REV_` INT,
  `CREATE_TIME_` DATETIME(6),
  `LINK_TYPE_` VARCHAR(255),
  `SCOPE_ID_` VARCHAR(255),
  `SUB_SCOPE_ID_` VARCHAR(255),
  `SCOPE_TYPE_` VARCHAR(255),
  `SCOPE_DEFINITION_ID_` VARCHAR(255),
  `PARENT_ELEMENT_ID_` VARCHAR(255),
  `REF_SCOPE_ID_` VARCHAR(255),
  `REF_SCOPE_TYPE_` VARCHAR(255),
  `REF_SCOPE_DEFINITION_ID_` VARCHAR(255),
  `ROOT_SCOPE_ID_` VARCHAR(255),
  `ROOT_SCOPE_TYPE_` VARCHAR(255),
  `HIERARCHY_TYPE_` VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of ACT_RU_ENTITYLINK
-- ----------------------------

-- ----------------------------
-- Table structure for ACT_RU_EVENT_SUBSCR
-- ----------------------------
DROP TABLE IF EXISTS `ACT_RU_EVENT_SUBSCR`;
CREATE TABLE `ACT_RU_EVENT_SUBSCR` (

  `ID_` VARCHAR(64) NOT NULL,
  `REV_` `INT`,
  `EVENT_TYPE_` VARCHAR(255) NOT NULL,
  `EVENT_NAME_` VARCHAR(255),
  `EXECUTION_ID_` VARCHAR(64),
  `PROC_INST_ID_` VARCHAR(64),
  `ACTIVITY_ID_` VARCHAR(64),
  `CONFIGURATION_` VARCHAR(255),
  `CREATED_` DATETIME(6) NOT NULL,
  `PROC_DEF_ID_` VARCHAR(64),
  `SUB_SCOPE_ID_` VARCHAR(64),
  `SCOPE_ID_` VARCHAR(64),
  `SCOPE_DEFINITION_ID_` VARCHAR(64),
  `SCOPE_DEFINITION_KEY_` VARCHAR(255),
  `SCOPE_TYPE_` VARCHAR(64),
  `LOCK_TIME_` DATETIME(6),
  `LOCK_OWNER_` VARCHAR(255),
  `TENANT_ID_` VARCHAR(255) DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of ACT_RU_EVENT_SUBSCR
-- ----------------------------

-- ----------------------------
-- Table structure for ACT_RU_EXECUTION
-- ----------------------------
DROP TABLE IF EXISTS `ACT_RU_EXECUTION`;
CREATE TABLE `ACT_RU_EXECUTION` (

  `ID_` VARCHAR(64) NOT NULL,
  `REV_` INT,
  `PROC_INST_ID_` VARCHAR(64),
  `BUSINESS_KEY_` VARCHAR(255),
  `PARENT_ID_` VARCHAR(64),
  `PROC_DEF_ID_` VARCHAR(64),
  `SUPER_EXEC_` VARCHAR(64),
  `ROOT_PROC_INST_ID_` VARCHAR(64),
  `ACT_ID_` VARCHAR(255),
  `IS_ACTIVE_` TINYINT,
  `IS_CONCURRENT_` TINYINT,
  `IS_SCOPE_` TINYINT,
  `IS_EVENT_SCOPE_` TINYINT,
  `IS_MI_ROOT_` TINYINT,
  `SUSPENSION_STATE_` INT,
  `CACHED_ENT_STATE_` INT,
  `TENANT_ID_` VARCHAR(255) DEFAULT '',
  `NAME_` VARCHAR(255),
  `START_ACT_ID_` VARCHAR(255),
  `START_TIME_` DATETIME(6),
  `START_USER_ID_` VARCHAR(255),
  `LOCK_TIME_` DATETIME(6),
  `LOCK_OWNER_` VARCHAR(255),
  `IS_COUNT_ENABLED_` TINYINT,
  `EVT_SUBSCR_COUNT_` INT,
  `TASK_COUNT_` INT,
  `JOB_COUNT_` INT,
  `TIMER_JOB_COUNT_` INT,
  `SUSP_JOB_COUNT_` INT,
  `DEADLETTER_JOB_COUNT_` INT,
  `EXTERNAL_WORKER_JOB_COUNT_` INT,
  `VAR_COUNT_` INT,
  `ID_LINK_COUNT_` INT,
  `CALLBACK_ID_` VARCHAR(255),
  `CALLBACK_TYPE_` VARCHAR(255),
  `REFERENCE_ID_` VARCHAR(255),
  `REFERENCE_TYPE_` VARCHAR(255),
  `PROPAGATED_STAGE_INST_ID_` VARCHAR(255),
  `BUSINESS_STATUS_` VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of ACT_RU_EXECUTION
-- ----------------------------

-- ----------------------------
-- Table structure for ACT_RU_EXTERNAL_JOB
-- ----------------------------
DROP TABLE IF EXISTS `ACT_RU_EXTERNAL_JOB`;
CREATE TABLE `ACT_RU_EXTERNAL_JOB` (

  `ID_` VARCHAR(64) NOT NULL,
  `REV_` INT,
  `CATEGORY_` VARCHAR(255),
  `TYPE_` VARCHAR(255) NOT NULL,
  `LOCK_EXP_TIME_` DATETIME(6),
  `LOCK_OWNER_` VARCHAR(255),
  `EXCLUSIVE_` TINYINT,
  `EXECUTION_ID_` VARCHAR(64),
  `PROCESS_INSTANCE_ID_` VARCHAR(64),
  `PROC_DEF_ID_` VARCHAR(64),
  `ELEMENT_ID_` VARCHAR(255),
  `ELEMENT_NAME_` VARCHAR(255),
  `SCOPE_ID_` VARCHAR(255),
  `SUB_SCOPE_ID_` VARCHAR(255),
  `SCOPE_TYPE_` VARCHAR(255),
  `SCOPE_DEFINITION_ID_` VARCHAR(255),
  `CORRELATION_ID_` VARCHAR(255),
  `RETRIES_` INT,
  `EXCEPTION_STACK_ID_` VARCHAR(64),
  `EXCEPTION_MSG_` TEXT,
  `DUEDATE_` DATETIME(6),
  `REPEAT_` VARCHAR(255),
  `HANDLER_TYPE_` VARCHAR(255),
  `HANDLER_CFG_` TEXT,
  `CUSTOM_VALUES_ID_` VARCHAR(64),
  `CREATE_TIME_` DATETIME(6),
  `TENANT_ID_` VARCHAR(255) DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of ACT_RU_EXTERNAL_JOB
-- ----------------------------

-- ----------------------------
-- Table structure for ACT_RU_HISTORY_JOB
-- ----------------------------
DROP TABLE IF EXISTS `ACT_RU_HISTORY_JOB`;
CREATE TABLE `ACT_RU_HISTORY_JOB` (

  `ID_` VARCHAR(64) NOT NULL,
  `REV_` INT,
  `LOCK_EXP_TIME_` DATETIME(6),
  `LOCK_OWNER_` VARCHAR(255),
  `RETRIES_` INT,
  `EXCEPTION_STACK_ID_` VARCHAR(64),
  `EXCEPTION_MSG_` TEXT,
  `HANDLER_TYPE_` VARCHAR(255),
  `HANDLER_CFG_` TEXT,
  `CUSTOM_VALUES_ID_` VARCHAR(64),
  `ADV_HANDLER_CFG_ID_` VARCHAR(64),
  `CREATE_TIME_` DATETIME(6),
  `SCOPE_TYPE_` VARCHAR(255),
  `TENANT_ID_` VARCHAR(255) DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of ACT_RU_HISTORY_JOB
-- ----------------------------

-- ----------------------------
-- Table structure for ACT_RU_IDENTITYLINK
-- ----------------------------
DROP TABLE IF EXISTS `ACT_RU_IDENTITYLINK`;
CREATE TABLE `ACT_RU_IDENTITYLINK` (

  `ID_` VARCHAR(64) NOT NULL,
  `REV_` INT,
  `GROUP_ID_` VARCHAR(255),
  `TYPE_` VARCHAR(255),
  `USER_ID_` VARCHAR(255),
  `TASK_ID_` VARCHAR(64),
  `PROC_INST_ID_` VARCHAR(64),
  `PROC_DEF_ID_` VARCHAR(64),
  `SCOPE_ID_` VARCHAR(255),
  `SUB_SCOPE_ID_` VARCHAR(255),
  `SCOPE_TYPE_` VARCHAR(255),
  `SCOPE_DEFINITION_ID_` VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of ACT_RU_IDENTITYLINK
-- ----------------------------

-- ----------------------------
-- Table structure for ACT_RU_JOB
-- ----------------------------
DROP TABLE IF EXISTS `ACT_RU_JOB`;
CREATE TABLE `ACT_RU_JOB` (

  `ID_` VARCHAR(64) NOT NULL,
  `REV_` INT,
  `CATEGORY_` VARCHAR(255),
  `TYPE_` VARCHAR(255) NOT NULL,
  `LOCK_EXP_TIME_` DATETIME(6),
  `LOCK_OWNER_` VARCHAR(255),
  `EXCLUSIVE_` TINYINT,
  `EXECUTION_ID_` VARCHAR(64),
  `PROCESS_INSTANCE_ID_` VARCHAR(64),
  `PROC_DEF_ID_` VARCHAR(64),
  `ELEMENT_ID_` VARCHAR(255),
  `ELEMENT_NAME_` VARCHAR(255),
  `SCOPE_ID_` VARCHAR(255),
  `SUB_SCOPE_ID_` VARCHAR(255),
  `SCOPE_TYPE_` VARCHAR(255),
  `SCOPE_DEFINITION_ID_` VARCHAR(255),
  `CORRELATION_ID_` VARCHAR(255),
  `RETRIES_` INT,
  `EXCEPTION_STACK_ID_` VARCHAR(64),
  `EXCEPTION_MSG_` TEXT,
  `DUEDATE_` DATETIME(6),
  `REPEAT_` VARCHAR(255),
  `HANDLER_TYPE_` VARCHAR(255),
  `HANDLER_CFG_` TEXT,
  `CUSTOM_VALUES_ID_` VARCHAR(64),
  `CREATE_TIME_` DATETIME(6),
  `TENANT_ID_` VARCHAR(255) DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of ACT_RU_JOB
-- ----------------------------

-- ----------------------------
-- Table structure for ACT_RU_SUSPENDED_JOB
-- ----------------------------
DROP TABLE IF EXISTS `ACT_RU_SUSPENDED_JOB`;
CREATE TABLE `ACT_RU_SUSPENDED_JOB` (

  `ID_` VARCHAR(64) NOT NULL,
  `REV_` INT,
  `CATEGORY_` VARCHAR(255),
  `TYPE_` VARCHAR(255) NOT NULL,
  `EXCLUSIVE_` TINYINT,
  `EXECUTION_ID_` VARCHAR(64),
  `PROCESS_INSTANCE_ID_` VARCHAR(64),
  `PROC_DEF_ID_` VARCHAR(64),
  `ELEMENT_ID_` VARCHAR(255),
  `ELEMENT_NAME_` VARCHAR(255),
  `SCOPE_ID_` VARCHAR(255),
  `SUB_SCOPE_ID_` VARCHAR(255),
  `SCOPE_TYPE_` VARCHAR(255),
  `SCOPE_DEFINITION_ID_` VARCHAR(255),
  `CORRELATION_ID_` VARCHAR(255),
  `RETRIES_` INT,
  `EXCEPTION_STACK_ID_` VARCHAR(64),
  `EXCEPTION_MSG_` TEXT,
  `DUEDATE_` DATETIME(6),
  `REPEAT_` VARCHAR(255),
  `HANDLER_TYPE_` VARCHAR(255),
  `HANDLER_CFG_` TEXT,
  `CUSTOM_VALUES_ID_` VARCHAR(64),
  `CREATE_TIME_` DATETIME(6),
  `TENANT_ID_` VARCHAR(255) DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of ACT_RU_SUSPENDED_JOB
-- ----------------------------

-- ----------------------------
-- Table structure for ACT_RU_TASK
-- ----------------------------
DROP TABLE IF EXISTS `ACT_RU_TASK`;
CREATE TABLE `ACT_RU_TASK` (

  `ID_` VARCHAR(64) NOT NULL,
  `REV_` INT,
  `EXECUTION_ID_` VARCHAR(64),
  `PROC_INST_ID_` VARCHAR(64),
  `PROC_DEF_ID_` VARCHAR(64),
  `TASK_DEF_ID_` VARCHAR(64),
  `SCOPE_ID_` VARCHAR(255),
  `SUB_SCOPE_ID_` VARCHAR(255),
  `SCOPE_TYPE_` VARCHAR(255),
  `SCOPE_DEFINITION_ID_` VARCHAR(255),
  `PROPAGATED_STAGE_INST_ID_` VARCHAR(255),
  `STATE_` VARCHAR(255),
  `NAME_` VARCHAR(255),
  `PARENT_TASK_ID_` VARCHAR(64),
  `DESCRIPTION_` TEXT,
  `TASK_DEF_KEY_` VARCHAR(255),
  `OWNER_` VARCHAR(255),
  `ASSIGNEE_` VARCHAR(255),
  `DELEGATION_` VARCHAR(64),
  `PRIORITY_` INT,
  `CREATE_TIME_` DATETIME(6),
  `IN_PROGRESS_TIME_` DATETIME(6),
  `IN_PROGRESS_STARTED_BY_` VARCHAR(255),
  `CLAIM_TIME_` DATETIME(6),
  `CLAIMED_BY_` VARCHAR(255),
  `SUSPENDED_TIME_` DATETIME(6),
  `SUSPENDED_BY_` VARCHAR(255),
  `IN_PROGRESS_DUE_DATE_` DATETIME(6),
  `DUE_DATE_` DATETIME(6),
  `CATEGORY_` VARCHAR(255),
  `SUSPENSION_STATE_` INT,
  `TENANT_ID_` VARCHAR(255) DEFAULT '',
  `FORM_KEY_` VARCHAR(255),
  `IS_COUNT_ENABLED_` TINYINT,
  `VAR_COUNT_` INT,
  `ID_LINK_COUNT_` INT,
  `SUB_TASK_COUNT_` INT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of ACT_RU_TASK
-- ----------------------------

-- ----------------------------
-- Table structure for ACT_RU_TIMER_JOB
-- ----------------------------
DROP TABLE IF EXISTS `ACT_RU_TIMER_JOB`;
CREATE TABLE `ACT_RU_TIMER_JOB` (

  `ID_` VARCHAR(64) NOT NULL,
  `REV_` INT,
  `CATEGORY_` VARCHAR(255),
  `TYPE_` VARCHAR(255) NOT NULL,
  `LOCK_EXP_TIME_` DATETIME(6),
  `LOCK_OWNER_` VARCHAR(255),
  `EXCLUSIVE_` TINYINT,
  `EXECUTION_ID_` VARCHAR(64),
  `PROCESS_INSTANCE_ID_` VARCHAR(64),
  `PROC_DEF_ID_` VARCHAR(64),
  `ELEMENT_ID_` VARCHAR(255),
  `ELEMENT_NAME_` VARCHAR(255),
  `SCOPE_ID_` VARCHAR(255),
  `SUB_SCOPE_ID_` VARCHAR(255),
  `SCOPE_TYPE_` VARCHAR(255),
  `SCOPE_DEFINITION_ID_` VARCHAR(255),
  `CORRELATION_ID_` VARCHAR(255),
  `RETRIES_` INT,
  `EXCEPTION_STACK_ID_` VARCHAR(64),
  `EXCEPTION_MSG_` TEXT,
  `DUEDATE_` DATETIME(6),
  `REPEAT_` VARCHAR(255),
  `HANDLER_TYPE_` VARCHAR(255),
  `HANDLER_CFG_` TEXT,
  `CUSTOM_VALUES_ID_` VARCHAR(64),
  `CREATE_TIME_` DATETIME(6),
  `TENANT_ID_` VARCHAR(255) DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of ACT_RU_TIMER_JOB
-- ----------------------------

-- ----------------------------
-- Table structure for ACT_RU_VARIABLE
-- ----------------------------
DROP TABLE IF EXISTS `ACT_RU_VARIABLE`;
CREATE TABLE `ACT_RU_VARIABLE` (

  `ID_` VARCHAR(64) NOT NULL,
  `REV_` INT,
  `TYPE_` VARCHAR(255) NOT NULL,
  `NAME_` VARCHAR(255) NOT NULL,
  `EXECUTION_ID_` VARCHAR(64),
  `PROC_INST_ID_` VARCHAR(64),
  `TASK_ID_` VARCHAR(64),
  `SCOPE_ID_` VARCHAR(255),
  `SUB_SCOPE_ID_` VARCHAR(255),
  `SCOPE_TYPE_` VARCHAR(255),
  `BYTEARRAY_ID_` VARCHAR(64),
  `DOUBLE_` DECIMAL(38,10),
  `LONG_` BIGINT,
  `TEXT_` TEXT,
  `TEXT2_` TEXT,
  `META_INFO_` TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of ACT_RU_VARIABLE
-- ----------------------------

-- ----------------------------
-- Table structure for FLW_CHANNEL_DEFINITION
-- ----------------------------
DROP TABLE IF EXISTS `FLW_CHANNEL_DEFINITION`;
CREATE TABLE `FLW_CHANNEL_DEFINITION` (

  `ID_` VARCHAR2(255) NOT NULL,
  `NAME_` VARCHAR2(255),
  `VERSION_` INT,
  `KEY_` VARCHAR2(255),
  `CATEGORY_` VARCHAR2(255),
  `TYPE_` VARCHAR2(255),
  `IMPLEMENTATION_` VARCHAR2(255),
  `DEPLOYMENT_ID_` VARCHAR2(255),
  `CREATE_TIME_` DATETIME(3),
  `TENANT_ID_` VARCHAR2(255),
  `RESOURCE_NAME_` VARCHAR2(255),
  `DESCRIPTION_` VARCHAR2(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of FLW_CHANNEL_DEFINITION
-- ----------------------------

-- ----------------------------
-- Table structure for FLW_EVENT_DEFINITION
-- ----------------------------
DROP TABLE IF EXISTS `FLW_EVENT_DEFINITION`;
CREATE TABLE `FLW_EVENT_DEFINITION` (

  `ID_` VARCHAR2(255) NOT NULL,
  `NAME_` VARCHAR2(255),
  `VERSION_` INT,
  `KEY_` VARCHAR2(255),
  `CATEGORY_` VARCHAR2(255),
  `DEPLOYMENT_ID_` VARCHAR2(255),
  `TENANT_ID_` VARCHAR2(255),
  `RESOURCE_NAME_` VARCHAR2(255),
  `DESCRIPTION_` VARCHAR2(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of FLW_EVENT_DEFINITION
-- ----------------------------

-- ----------------------------
-- Table structure for FLW_EVENT_DEPLOYMENT
-- ----------------------------
DROP TABLE IF EXISTS `FLW_EVENT_DEPLOYMENT`;
CREATE TABLE `FLW_EVENT_DEPLOYMENT` (

  `ID_` VARCHAR2(255) NOT NULL,
  `NAME_` VARCHAR2(255),
  `CATEGORY_` VARCHAR2(255),
  `DEPLOY_TIME_` DATETIME(3),
  `TENANT_ID_` VARCHAR2(255),
  `PARENT_DEPLOYMENT_ID_` VARCHAR2(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of FLW_EVENT_DEPLOYMENT
-- ----------------------------

-- ----------------------------
-- Table structure for FLW_EVENT_RESOURCE
-- ----------------------------
DROP TABLE IF EXISTS `FLW_EVENT_RESOURCE`;
CREATE TABLE `FLW_EVENT_RESOURCE` (

  `ID_` VARCHAR2(255) NOT NULL,
  `NAME_` VARCHAR2(255),
  `DEPLOYMENT_ID_` VARCHAR2(255),
  `RESOURCE_BYTES_` LONGBLOB
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of FLW_EVENT_RESOURCE
-- ----------------------------

-- ----------------------------
-- Table structure for FLW_RU_BATCH
-- ----------------------------
DROP TABLE IF EXISTS `FLW_RU_BATCH`;
CREATE TABLE `FLW_RU_BATCH` (

  `ID_` VARCHAR(64) NOT NULL,
  `REV_` INT,
  `TYPE_` VARCHAR(64) NOT NULL,
  `SEARCH_KEY_` VARCHAR(255),
  `SEARCH_KEY2_` VARCHAR(255),
  `CREATE_TIME_` DATETIME(6) NOT NULL,
  `COMPLETE_TIME_` DATETIME(6),
  `STATUS_` VARCHAR(255),
  `BATCH_DOC_ID_` VARCHAR(64),
  `TENANT_ID_` VARCHAR(255) DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of FLW_RU_BATCH
-- ----------------------------

-- ----------------------------
-- Table structure for FLW_RU_BATCH_PART
-- ----------------------------
DROP TABLE IF EXISTS `FLW_RU_BATCH_PART`;
CREATE TABLE `FLW_RU_BATCH_PART` (

  `ID_` VARCHAR(64) NOT NULL,
  `REV_` INT,
  `BATCH_ID_` VARCHAR(64),
  `TYPE_` VARCHAR(64) NOT NULL,
  `SCOPE_ID_` VARCHAR(64),
  `SUB_SCOPE_ID_` VARCHAR(64),
  `SCOPE_TYPE_` VARCHAR(64),
  `SEARCH_KEY_` VARCHAR(255),
  `SEARCH_KEY2_` VARCHAR(255),
  `CREATE_TIME_` DATETIME(6) NOT NULL,
  `COMPLETE_TIME_` DATETIME(6),
  `STATUS_` VARCHAR(255),
  `RESULT_DOC_ID_` VARCHAR(64),
  `TENANT_ID_` VARCHAR(255) DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of FLW_RU_BATCH_PART
-- ----------------------------

-- ----------------------------
-- Table structure for SYS_DEPT
-- ----------------------------
DROP TABLE IF EXISTS `SYS_DEPT`;
CREATE TABLE `SYS_DEPT` (

  `ID` BIGINT NOT NULL COMMENT '主键',
  `PARENT_ID` BIGINT COMMENT '上级部门ID',
  `DEPT_CODE` VARCHAR(64) NOT NULL COMMENT '部门编码',
  `DEPT_NAME` VARCHAR(128) NOT NULL COMMENT '部门名称',
  `SORT_NO` INT DEFAULT 0 COMMENT '排序号',
  `STATUS` INT DEFAULT 1 COMMENT '状态',
  `REMARK` VARCHAR(500) COMMENT '备注',
  `CREATE_TIME` DATETIME(6) COMMENT '创建时间',
  `UPDATE_TIME` DATETIME(6) COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='部门表';
-- ----------------------------
-- Records of SYS_DEPT
-- ----------------------------
INSERT INTO `SYS_DEPT` VALUES (1001, NULL, 'HEAD', '总部', 1, 1, '默认根部门', '2026-07-15 18:31:57.273000', '2026-07-15 18:31:57.273000');

-- ----------------------------
-- Table structure for SYS_PERMISSION
-- ----------------------------
DROP TABLE IF EXISTS `SYS_PERMISSION`;
CREATE TABLE `SYS_PERMISSION` (

  `ID` BIGINT NOT NULL COMMENT '主键',
  `PARENT_ID` BIGINT COMMENT '上级权限ID',
  `PERMISSION_CODE` VARCHAR(128) NOT NULL COMMENT '权限编码',
  `PERMISSION_NAME` VARCHAR(128) NOT NULL COMMENT '权限名称',
  `PERMISSION_TYPE` VARCHAR(32) COMMENT '权限类型',
  `ROUTE_PATH` VARCHAR(255) COMMENT '路由地址',
  `SORT_NO` INT DEFAULT 0 COMMENT '排序号',
  `STATUS` INT DEFAULT 1 COMMENT '状态',
  `REMARK` VARCHAR(500) COMMENT '备注',
  `CREATE_TIME` DATETIME(6) COMMENT '创建时间',
  `UPDATE_TIME` DATETIME(6) COMMENT '更新时间',
  `ICON` VARCHAR(128) COMMENT '图标'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='权限表';
-- ----------------------------
-- Records of SYS_PERMISSION
-- ----------------------------
INSERT INTO `SYS_PERMISSION` VALUES (1101, NULL, 'menu:home', '首页', 'MENU', '/home', 1, 1, '首页菜单权限', '2026-07-17 14:44:44.973000', '2026-07-17 14:44:44.974000', 'el-icon-house');
INSERT INTO `SYS_PERMISSION` VALUES (1102, NULL, 'menu:deploy', '部署中心', 'MENU', '/deploy', 2, 1, '部署中心菜单权限', '2026-07-17 14:44:44.979000', '2026-07-17 14:44:44.979000', 'el-icon-upload2');
INSERT INTO `SYS_PERMISSION` VALUES (1103, NULL, 'menu:model', '模型中心', 'MENU', '/model', 3, 1, '模型中心菜单权限', '2026-07-17 14:44:44.981000', '2026-07-17 14:44:44.981000', 'el-icon-collection');
INSERT INTO `SYS_PERMISSION` VALUES (1104, NULL, 'menu:designer', '流程设计', 'MENU', '/designer', 4, 1, '流程设计菜单权限', '2026-07-17 14:44:44.982000', '2026-07-17 14:44:44.982000', 'el-icon-edit-outline');
INSERT INTO `SYS_PERMISSION` VALUES (1105, NULL, 'menu:process', '流程中心', 'MENU', '/process', 5, 1, '流程中心菜单权限', '2026-07-17 14:44:44.983000', '2026-07-17 14:44:44.983000', 'el-icon-s-operation');
INSERT INTO `SYS_PERMISSION` VALUES (1106, NULL, 'menu:task', '任务中心', 'MENU', '/task', 6, 1, '任务中心菜单权限', '2026-07-17 14:44:44.985000', '2026-07-17 14:44:44.985000', 'el-icon-s-check');
INSERT INTO `SYS_PERMISSION` VALUES (1107, NULL, 'menu:sys:user', '用户管理', 'MENU', '/system/user', 7, 1, '用户管理菜单权限', '2026-07-17 14:44:44.985000', '2026-07-17 14:44:44.985000', 'el-icon-user');
INSERT INTO `SYS_PERMISSION` VALUES (1108, NULL, 'menu:sys:role', '角色管理', 'MENU', '/system/role', 8, 1, '角色管理菜单权限', '2026-07-17 14:44:44.987000', '2026-07-17 14:44:44.987000', 'el-icon-s-custom');
INSERT INTO `SYS_PERMISSION` VALUES (1109, NULL, 'menu:sys:permission', '权限管理', 'MENU', '/system/permission', 9, 1, '权限管理菜单权限', '2026-07-17 14:44:44.989000', '2026-07-17 14:44:44.989000', 'el-icon-key');
INSERT INTO `SYS_PERMISSION` VALUES (1110, NULL, 'menu:sys:dept', '部门管理', 'MENU', '/system/dept', 10, 1, '部门管理菜单权限', '2026-07-17 14:44:44.989000', '2026-07-17 14:44:44.989000', 'el-icon-office-building');
INSERT INTO `SYS_PERMISSION` VALUES (1111, 1107, 'sys:user:view', '查看用户', 'BUTTON', NULL, 11, 1, '用户查询权限', '2026-07-17 14:44:44.990000', '2026-07-17 14:44:44.990000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1112, 1107, 'sys:user:add', '新增用户', 'BUTTON', NULL, 12, 1, '用户新增权限', '2026-07-17 14:44:44.991000', '2026-07-17 14:44:44.991000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1113, 1107, 'sys:user:edit', '修改用户', 'BUTTON', NULL, 13, 1, '用户修改权限', '2026-07-17 14:44:44.992000', '2026-07-17 14:44:44.992000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1114, 1107, 'sys:user:delete', '删除用户', 'BUTTON', NULL, 14, 1, '用户删除权限', '2026-07-17 14:44:44.992000', '2026-07-17 14:44:44.992000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1115, 1108, 'sys:role:view', '查看角色', 'BUTTON', NULL, 15, 1, '角色查询权限', '2026-07-17 14:44:44.993000', '2026-07-17 14:44:44.993000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1116, 1108, 'sys:role:add', '新增角色', 'BUTTON', NULL, 16, 1, '角色新增权限', '2026-07-17 14:44:44.993000', '2026-07-17 14:44:44.993000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1117, 1108, 'sys:role:edit', '修改角色', 'BUTTON', NULL, 17, 1, '角色修改权限', '2026-07-17 14:44:44.994000', '2026-07-17 14:44:44.994000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1118, 1108, 'sys:role:delete', '删除角色', 'BUTTON', NULL, 18, 1, '角色删除权限', '2026-07-17 14:44:44.995000', '2026-07-17 14:44:44.995000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1119, 1109, 'sys:permission:view', '查看权限', 'BUTTON', NULL, 19, 1, '权限查询权限', '2026-07-17 14:44:44.995000', '2026-07-17 14:44:44.995000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1120, 1109, 'sys:permission:add', '新增权限', 'BUTTON', NULL, 20, 1, '权限新增权限', '2026-07-17 14:44:44.996000', '2026-07-17 14:44:44.996000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1121, 1109, 'sys:permission:edit', '修改权限', 'BUTTON', NULL, 21, 1, '权限修改权限', '2026-07-17 14:44:44.997000', '2026-07-17 14:44:44.997000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1122, 1109, 'sys:permission:delete', '删除权限', 'BUTTON', NULL, 22, 1, '权限删除权限', '2026-07-17 14:44:44.997000', '2026-07-17 14:44:44.997000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1123, 1110, 'sys:dept:view', '查看部门', 'BUTTON', NULL, 23, 1, '部门查询权限', '2026-07-17 14:44:44.998000', '2026-07-17 14:44:44.998000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1124, 1110, 'sys:dept:add', '新增部门', 'BUTTON', NULL, 24, 1, '部门新增权限', '2026-07-17 14:44:45.000000', '2026-07-17 14:44:45.000000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1125, 1110, 'sys:dept:edit', '修改部门', 'BUTTON', NULL, 25, 1, '部门修改权限', '2026-07-17 14:44:45.002000', '2026-07-17 14:44:45.002000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1126, 1110, 'sys:dept:delete', '删除部门', 'BUTTON', NULL, 26, 1, '部门删除权限', '2026-07-17 14:44:45.002000', '2026-07-17 14:44:45.002000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1127, NULL, '*:*:*', '系统全量权限', 'BUTTON', NULL, 99, 1, '管理员全量权限', '2026-07-17 14:44:45.003000', '2026-07-17 14:44:45.003000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1128, NULL, 'client:view', '客户端管理', 'MENU', '/client', 27, 1, '查看已注册客户端信息', '2026-07-17 14:44:45.003000', '2026-07-17 14:44:45.003000', 'el-icon-connection');
INSERT INTO `SYS_PERMISSION` VALUES (1129, 1128, 'client:delete', '移除客户端', 'BUTTON', NULL, 28, 1, '移除客户端注册信息', '2026-07-17 14:44:45.004000', '2026-07-17 14:44:45.004000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1130, 1102, 'deploy:tab:create', '创建部署', 'TAB', NULL, 1, 1, '部署中心创建部署', '2026-07-17 14:44:45.009000', '2026-07-17 14:44:45.009000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1131, 1102, 'deploy:tab:list', '流程列表', 'TAB', NULL, 2, 1, '部署中心流程列表', '2026-07-17 14:44:45.010000', '2026-07-17 14:44:45.010000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1132, 1103, 'model:tab:create', '创建模型', 'TAB', NULL, 1, 1, '模型中心创建模型', '2026-07-17 14:44:45.010000', '2026-07-17 14:44:45.011000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1133, 1103, 'model:tab:list', '模型列表', 'TAB', NULL, 2, 1, '模型中心模型列表', '2026-07-17 14:44:45.012000', '2026-07-17 14:44:45.012000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1134, 1105, 'process:tab:create', '创建流程', 'TAB', NULL, 1, 1, '流程中心创建流程', '2026-07-17 14:44:45.012000', '2026-07-17 14:44:45.012000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1135, 1105, 'process:tab:list', '流程列表', 'TAB', NULL, 2, 1, '流程中心流程列表', '2026-07-17 14:44:45.013000', '2026-07-17 14:44:45.013000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1136, 1102, 'deploy:refresh', '刷新部署', 'BUTTON', NULL, 10, 1, '刷新部署中心数据', '2026-07-17 14:44:45.014000', '2026-07-17 14:44:45.014000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1137, 1102, 'deploy:create', '提交部署', 'BUTTON', NULL, 11, 1, '提交流程部署', '2026-07-17 14:44:45.015000', '2026-07-17 14:44:45.015000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1138, 1102, 'deploy:start', '发起审批', 'BUTTON', NULL, 12, 1, '从部署发起审批', '2026-07-17 14:44:45.016000', '2026-07-17 14:44:45.016000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1139, 1102, 'deploy:edit', '编辑部署', 'BUTTON', NULL, 13, 1, '编辑部署流程定义', '2026-07-17 14:44:45.018000', '2026-07-17 14:44:45.018000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1140, 1102, 'deploy:view', '查看部署', 'BUTTON', NULL, 14, 1, '查看部署详情', '2026-07-17 14:44:45.019000', '2026-07-17 14:44:45.019000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1141, 1102, 'deploy:preview', '预览部署', 'BUTTON', NULL, 15, 1, '预览部署流程图', '2026-07-17 14:44:45.020000', '2026-07-17 14:44:45.020000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1142, 1102, 'deploy:binding', '修改部署绑定', 'BUTTON', NULL, 16, 1, '修改部署客户端绑定', '2026-07-17 14:44:45.020000', '2026-07-17 14:44:45.020000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1143, 1102, 'deploy:delete', '删除部署', 'BUTTON', NULL, 17, 1, '删除部署数据', '2026-07-17 14:44:45.021000', '2026-07-17 14:44:45.021000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1144, 1103, 'model:refresh', '刷新模型', 'BUTTON', NULL, 10, 1, '刷新模型中心数据', '2026-07-17 14:44:45.022000', '2026-07-17 14:44:45.022000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1145, 1103, 'model:create', '创建模型', 'BUTTON', NULL, 11, 1, '创建流程模型', '2026-07-17 14:44:45.022000', '2026-07-17 14:44:45.022000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1146, 1103, 'model:edit', '修改模型', 'BUTTON', NULL, 12, 1, '修改流程模型', '2026-07-17 14:44:45.023000', '2026-07-17 14:44:45.023000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1147, 1103, 'model:preview', '预览模型', 'BUTTON', NULL, 13, 1, '预览流程模型', '2026-07-17 14:44:45.023000', '2026-07-17 14:44:45.023000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1148, 1103, 'model:deploy', '部署模型', 'BUTTON', NULL, 14, 1, '部署流程模型', '2026-07-17 14:44:45.023000', '2026-07-17 14:44:45.023000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1149, 1103, 'model:delete', '删除模型', 'BUTTON', NULL, 15, 1, '删除流程模型', '2026-07-17 14:44:45.024000', '2026-07-17 14:44:45.024000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1150, 1105, 'process:refresh', '刷新流程', 'BUTTON', NULL, 10, 1, '刷新流程中心数据', '2026-07-17 14:44:45.024000', '2026-07-17 14:44:45.024000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1151, 1105, 'process:view', '查看流程', 'BUTTON', NULL, 11, 1, '查看流程详情', '2026-07-17 14:44:45.025000', '2026-07-17 14:44:45.025000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1152, 1105, 'process:submit', '提交流程', 'BUTTON', NULL, 12, 1, '提交草稿流程', '2026-07-17 14:44:45.025000', '2026-07-17 14:44:45.025000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1153, 1105, 'process:delete', '删除流程', 'BUTTON', NULL, 13, 1, '删除流程申请', '2026-07-17 14:44:45.026000', '2026-07-17 14:44:45.026000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1154, 1105, 'process:action:draft', '保存草稿', 'BUTTON', NULL, 14, 1, '流程表单保存草稿按钮', '2026-07-17 14:44:45.026000', '2026-07-17 14:44:45.026000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1155, 1105, 'process:action:submit', '提交申请', 'BUTTON', NULL, 15, 1, '流程表单提交申请按钮', '2026-07-17 14:44:45.026000', '2026-07-17 14:44:45.026000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1156, 1105, 'process:action:reset', '重置流程表单', 'BUTTON', NULL, 16, 1, '流程表单重置按钮', '2026-07-17 14:44:45.027000', '2026-07-17 14:44:45.027000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1157, 1106, 'task:refresh', '刷新任务', 'BUTTON', NULL, 10, 1, '刷新任务中心数据', '2026-07-17 14:44:45.028000', '2026-07-17 14:44:45.028000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1158, 1106, 'task:view', '查看任务', 'BUTTON', NULL, 11, 1, '查看任务流程图', '2026-07-17 14:44:45.028000', '2026-07-17 14:44:45.028000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1159, 1106, 'task:approve', '办理任务', 'BUTTON', NULL, 12, 1, '办理审批任务', '2026-07-17 14:44:45.029000', '2026-07-17 14:44:45.029000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1160, 1106, 'task:delete', '删除任务', 'BUTTON', NULL, 13, 1, '删除任务', '2026-07-17 14:44:45.030000', '2026-07-17 14:44:45.030000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1161, 1128, 'client:refresh', '刷新客户端', 'BUTTON', NULL, 10, 1, '刷新客户端数据', '2026-07-17 14:44:45.031000', '2026-07-17 14:44:45.031000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1162, 1128, 'client:detect', '检测客户端', 'BUTTON', NULL, 11, 1, '检测客户端存活状态', '2026-07-17 14:44:45.032000', '2026-07-17 14:44:45.032000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1163, 1105, 'process:status:tag', '流程状态', 'TAG', NULL, 30, 1, '流程中心状态资源', '2026-07-17 14:44:45.036000', '2026-07-17 14:44:45.036000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1164, 1104, 'designer:export:bpmn', '导出 BPMN', 'BUTTON', NULL, 10, 1, '导出 BPMN 文件', '2026-07-17 15:22:28.490000', '2026-07-17 15:22:28.490000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1165, 1104, 'designer:export:bpmn-xml', '导出 BPMN.XML', 'BUTTON', NULL, 11, 1, '导出 BPMN.XML 文件', '2026-07-17 15:22:28.491000', '2026-07-17 15:22:28.491000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1166, 1104, 'designer:export:png', '导出 PNG', 'BUTTON', NULL, 12, 1, '导出 PNG 图片', '2026-07-17 15:22:28.499000', '2026-07-17 15:22:28.499000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1167, 1104, 'designer:canvas:center', '居中显示', 'BUTTON', NULL, 13, 1, '将画布内容居中显示', '2026-07-17 15:22:28.502000', '2026-07-17 15:22:28.502000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1168, 1104, 'designer:canvas:reset', '清空画布', 'BUTTON', NULL, 14, 1, '清空画布内容', '2026-07-17 15:22:28.504000', '2026-07-17 15:22:28.504000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1169, 1104, 'designer:canvas:refresh', '刷新画布', 'BUTTON', NULL, 15, 1, '刷新画布内容', '2026-07-17 15:22:28.508000', '2026-07-17 15:22:28.508000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1170, NULL, 'menu:form', '表单设计', 'MENU', '/form-designer', 5, 1, '表单设计菜单权限', '2026-07-20 11:31:28.312000', '2026-07-20 11:31:28.313000', 'el-icon-tickets');
INSERT INTO `SYS_PERMISSION` VALUES (1171, 1170, 'form:save', '保存表单方案', 'BUTTON', NULL, 10, 1, '保存表单设计方案', '2026-07-20 11:31:59.281000', '2026-07-20 11:31:59.281000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1172, 1170, 'form:load', '载入表单方案', 'BUTTON', NULL, 11, 1, '载入表单设计方案', '2026-07-20 11:31:59.285000', '2026-07-20 11:31:59.285000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1173, 1170, 'form:export', '导出表单JSON', 'BUTTON', NULL, 12, 1, '导出表单设计JSON', '2026-07-20 11:31:59.286000', '2026-07-20 11:31:59.286000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1174, 1170, 'form:import', '导入表单JSON', 'BUTTON', NULL, 13, 1, '导入表单设计JSON', '2026-07-20 11:31:59.287000', '2026-07-20 11:31:59.287000', NULL);
INSERT INTO `SYS_PERMISSION` VALUES (1175, 1170, 'form:jump:designer', '跳转流程设计', 'BUTTON', NULL, 14, 1, '从表单设计跳转流程设计', '2026-07-20 11:31:59.289000', '2026-07-20 11:31:59.289000', NULL);

-- ----------------------------
-- Table structure for SYS_ROLE
-- ----------------------------
DROP TABLE IF EXISTS `SYS_ROLE`;
CREATE TABLE `SYS_ROLE` (

  `ID` BIGINT NOT NULL COMMENT '主键',
  `ROLE_CODE` VARCHAR(64) NOT NULL COMMENT '角色编码',
  `ROLE_NAME` VARCHAR(64) NOT NULL COMMENT '角色名称',
  `SORT_NO` INT DEFAULT 0 COMMENT '排序号',
  `STATUS` INT DEFAULT 1 COMMENT '状态',
  `REMARK` VARCHAR(500) COMMENT '备注',
  `CREATE_TIME` DATETIME(6) COMMENT '创建时间',
  `UPDATE_TIME` DATETIME(6) COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色表';
-- ----------------------------
-- Records of SYS_ROLE
-- ----------------------------
INSERT INTO `SYS_ROLE` VALUES (1001, 'ADMIN', '系统管理员', 1, 1, '默认管理员角色', '2026-07-15 18:31:57.279000', '2026-07-20 11:32:22.356737');
INSERT INTO `SYS_ROLE` VALUES (2077959494374789122, '测试用户', '测试用户', 2, 1, NULL, '2026-07-17 11:32:04.737287', '2026-07-17 15:31:29.905303');
INSERT INTO `SYS_ROLE` VALUES (2078009602017673217, 'a', 'a', 0, 1, NULL, '2026-07-17 14:51:11.322740', '2026-07-17 14:51:11.322740');

-- ----------------------------
-- Table structure for SYS_ROLE_PERMISSION
-- ----------------------------
DROP TABLE IF EXISTS `SYS_ROLE_PERMISSION`;
CREATE TABLE `SYS_ROLE_PERMISSION` (

  `ROLE_ID` BIGINT NOT NULL COMMENT '角色ID',
  `PERMISSION_ID` BIGINT NOT NULL COMMENT '权限ID',
  `CREATE_TIME` DATETIME(6) COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色权限关联表';
-- ----------------------------
-- Records of SYS_ROLE_PERMISSION
-- ----------------------------
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (2077959494374789122, 1101, '2026-07-17 15:31:29.905303');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (2077959494374789122, 1131, '2026-07-17 15:31:29.905303');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (2077959494374789122, 1136, '2026-07-17 15:31:29.905303');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (2077959494374789122, 1139, '2026-07-17 15:31:29.905303');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (2077959494374789122, 1140, '2026-07-17 15:31:29.905303');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (2077959494374789122, 1141, '2026-07-17 15:31:29.905303');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (2077959494374789122, 1132, '2026-07-17 15:31:29.905303');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (2077959494374789122, 1133, '2026-07-17 15:31:29.905303');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (2077959494374789122, 1147, '2026-07-17 15:31:29.905303');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (2077959494374789122, 1166, '2026-07-17 15:31:29.905303');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (2077959494374789122, 1167, '2026-07-17 15:31:29.905303');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (2077959494374789122, 1168, '2026-07-17 15:31:29.905303');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (2077959494374789122, 1169, '2026-07-17 15:31:29.905303');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (2077959494374789122, 1135, '2026-07-17 15:31:29.905303');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (2077959494374789122, 1150, '2026-07-17 15:31:29.905303');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (2077959494374789122, 1151, '2026-07-17 15:31:29.905303');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (2077959494374789122, 1157, '2026-07-17 15:31:29.905303');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (2077959494374789122, 1158, '2026-07-17 15:31:29.905303');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1101, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1102, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1130, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1131, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1136, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1137, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1138, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1139, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1140, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1141, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1142, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1143, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1103, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1132, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1133, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1144, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1145, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1146, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1147, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1148, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1149, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1104, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1164, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1165, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1166, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1167, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1168, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1169, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1105, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1134, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1135, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1150, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1151, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1152, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1153, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1154, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1155, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1156, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1163, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1106, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1157, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1158, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1159, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1160, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1107, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1111, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1112, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1113, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1114, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1108, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1115, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1116, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1117, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1118, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1109, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1119, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1120, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1121, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1122, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1110, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1123, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1124, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1125, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1126, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1127, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1161, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1162, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1170, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1171, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1172, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1173, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1174, '2026-07-20 11:32:22.366703');
INSERT INTO `SYS_ROLE_PERMISSION` VALUES (1001, 1175, '2026-07-20 11:32:22.366703');

-- ----------------------------
-- Table structure for SYS_USER
-- ----------------------------
DROP TABLE IF EXISTS `SYS_USER`;
CREATE TABLE `SYS_USER` (

  `ID` BIGINT NOT NULL COMMENT '主键',
  `DEPT_ID` BIGINT COMMENT '所属部门ID',
  `USERNAME` VARCHAR(64) NOT NULL COMMENT '用户名',
  `PASSWORD_HASH` VARCHAR(64) NOT NULL COMMENT '密码摘要',
  `REAL_NAME` VARCHAR(64) NOT NULL COMMENT '姓名',
  `MOBILE` VARCHAR(32) COMMENT '手机号',
  `EMAIL` VARCHAR(128) COMMENT '邮箱',
  `STATUS` INT DEFAULT 1 COMMENT '状态',
  `LAST_LOGIN_TIME` DATETIME(6) COMMENT '最后登录时间',
  `CREATE_TIME` DATETIME(6) COMMENT '创建时间',
  `UPDATE_TIME` DATETIME(6) COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';
-- ----------------------------
-- Records of SYS_USER
-- ----------------------------
INSERT INTO `SYS_USER` VALUES (1001, 1001, 'admin', '0192023A7BBD73250516F069DF18B500', '系统管理员', NULL, NULL, 1, '2026-07-24 10:59:04.130107', '2026-07-15 18:31:57.285000', '2026-07-24 10:59:04.130107');
INSERT INTO `SYS_USER` VALUES (2077960115920310273, 1001, 'test', '098f6bcd4621d373cade4e832627b4f6', 'test', NULL, NULL, 1, '2026-07-17 15:34:52.016579', '2026-07-17 11:34:32.924682', '2026-07-17 15:34:52.016579');

-- ----------------------------
-- Table structure for SYS_USER_ROLE
-- ----------------------------
DROP TABLE IF EXISTS `SYS_USER_ROLE`;
CREATE TABLE `SYS_USER_ROLE` (

  `USER_ID` BIGINT NOT NULL COMMENT '用户ID',
  `ROLE_ID` BIGINT NOT NULL COMMENT '角色ID',
  `CREATE_TIME` DATETIME(6) COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户角色关联表';
-- ----------------------------
-- Records of SYS_USER_ROLE
-- ----------------------------
INSERT INTO `SYS_USER_ROLE` VALUES (1001, 1001, '2026-07-15 18:31:57.361000');
INSERT INTO `SYS_USER_ROLE` VALUES (2077960115920310273, 2077959494374789122, '2026-07-17 11:34:32.936267');

-- ----------------------------
-- Table structure for WCDK_PROCESS_CLIENT
-- ----------------------------
DROP TABLE IF EXISTS `WCDK_PROCESS_CLIENT`;
CREATE TABLE `WCDK_PROCESS_CLIENT` (

  `ID` BIGINT NOT NULL COMMENT '主键',
  `CLIENT_ID` VARCHAR(64) NOT NULL COMMENT '客户端标识',
  `CLIENT_NAME` VARCHAR(128) NOT NULL COMMENT '客户端名称',
  `CALLBACK_URL` VARCHAR(500) COMMENT '回调地址',
  `AUTH_FLG` LONGTEXT COMMENT '身份标识',
  `CREATE_TIME` DATETIME(6) COMMENT '创建时间',
  `UPDATE_TIME` DATETIME(6) COMMENT '更新时间',
  `SERVICE_NAME` VARCHAR2(255) COMMENT '服务名'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='客户端回调绑定表';
-- ----------------------------
-- Records of WCDK_PROCESS_CLIENT
-- ----------------------------
INSERT INTO `WCDK_PROCESS_CLIENT` VALUES (2080227595100692482, 'wcdk-process-demo', 'wcdk-process-demo', NULL, 'WCDK', '2026-07-23 17:44:42.018344', '2026-07-23 17:48:26.845909', 'wcdk-process-demo');

-- ----------------------------
-- Table structure for WCDK_PROCESS_CLIENT_PROCESS
-- ----------------------------
DROP TABLE IF EXISTS `WCDK_PROCESS_CLIENT_PROCESS`;
CREATE TABLE `WCDK_PROCESS_CLIENT_PROCESS` (

  `ID` BIGINT NOT NULL COMMENT '主键',
  `CLIENT_ID` VARCHAR(64) NOT NULL COMMENT '客户端标识',
  `PROCESS_BEAN_NAME` VARCHAR(128) COMMENT '流程处理器名称',
  `PROCESS_DEFINITION_ID` VARCHAR(128) COMMENT '流程定义ID',
  `PROCESS_NAME` VARCHAR(255) COMMENT '回调Bean',
  `EXCUTE_PARAM` VARCHAR(1000) COMMENT '执行参数',
  `CREATE_TIME` DATETIME(6) COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='客户端流程绑定表';
-- ----------------------------
-- Records of WCDK_PROCESS_CLIENT_PROCESS
-- ----------------------------
INSERT INTO `WCDK_PROCESS_CLIENT_PROCESS` VALUES (2080228538244472834, 'wcdk-process-demo', 'test', NULL, NULL, NULL, '2026-07-23 17:48:26.845909');

-- ----------------------------
-- Table structure for WCDK_PROCESS_DEFINITION_META
-- ----------------------------
DROP TABLE IF EXISTS `WCDK_PROCESS_DEFINITION_META`;
CREATE TABLE `WCDK_PROCESS_DEFINITION_META` (

  `ID` BIGINT NOT NULL COMMENT '主键',
  `PROCESS_DEFINITION_ID` VARCHAR(128) NOT NULL COMMENT 'Flowable流程定义ID',
  `PROCESS_DEFINITION_KEY` VARCHAR(128) COMMENT 'Flowable流程定义KEY',
  `PROCESS_DEFINITION_VERSION` INT COMMENT 'Flowable流程定义版本',
  `DEPLOYMENT_ID` VARCHAR(128) COMMENT 'Flowable部署ID',
  `INVALID_STATUS` INT DEFAULT 0 NOT NULL COMMENT '作废状态：0生效，1已作废',
  `CREATE_TIME` DATETIME(6) COMMENT '创建时间',
  `UPDATE_TIME` DATETIME(6) COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='流程定义元数据表';
-- ----------------------------
-- Records of WCDK_PROCESS_DEFINITION_META
-- ----------------------------

-- ----------------------------
-- Table structure for WCDK_PROCESS_FORM
-- ----------------------------
DROP TABLE IF EXISTS `WCDK_PROCESS_FORM`;
CREATE TABLE `WCDK_PROCESS_FORM` (

  `ID` BIGINT NOT NULL COMMENT '主键',
  `FORM_KEY` VARCHAR(128) NOT NULL COMMENT '表单标识，对应Flowable表单key',
  `FORM_NAME` VARCHAR(255) NOT NULL COMMENT '表单名称',
  `FORM_VERSION` INT DEFAULT 1 NOT NULL COMMENT '表单版本',
  `FORM_SCHEMA_JSON` LONGTEXT NOT NULL COMMENT '表单设计JSON',
  `RESOURCE_NAME` VARCHAR(255) COMMENT '表单资源名称',
  `TENANT_ID` VARCHAR(128) DEFAULT '' NOT NULL COMMENT '租户ID',
  `STATUS` INT DEFAULT 1 NOT NULL COMMENT '状态：1启用，0停用',
  `REMARK` VARCHAR(500) COMMENT '备注',
  `CREATE_USER` VARCHAR(64) COMMENT '创建人',
  `CREATE_TIME` DATETIME(6) COMMENT '创建时间',
  `UPDATE_TIME` DATETIME(6) COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='流程表单定义表';
-- ----------------------------
-- Records of WCDK_PROCESS_FORM
-- ----------------------------
INSERT INTO `WCDK_PROCESS_FORM` VALUES (2079867318517702657, '下拉列表数据源演示', '组件用法演示01', 1, '[{"id":"select_1784713845035_6284","type":"select","componentType":"select","x":88.283447265625,"y":78.22589111328125,"width":152,"height":30,"sortOrder":1,"fieldKey":"field_1","placeholder":"请选择下拉选择","required":false,"readOnly":false,"defaultValue":"","options":[{"label":"男","value":"1"},{"label":"女","value":"0"}],"dataSourceType":"request","presetOptionsText":"[{\"label\":\"男\",\"value\":\"1\"},{\"label\":\"女\",\"value\":\"0\"}]","dataSourceUrl":"/process/request/list?pageNum=1&pageSize=10","dataSourceMethod":"GET","dataSourceScript":"(data)=>{\n return data.data.records.map(\n  function (item){\n   return { label: item.taskName, value: item.id }\n}\n)\n}","color":"","triggerMode":"click","function":"","httpMethod":"","param":""}]', '下拉列表数据源演示.json', '', 1, NULL, 'admin', '2026-07-22 17:53:05.421528', '2026-07-22 18:06:33.110466');

-- ----------------------------
-- Table structure for WCDK_PROCESS_FORM_BINDING
-- ----------------------------
DROP TABLE IF EXISTS `WCDK_PROCESS_FORM_BINDING`;
CREATE TABLE `WCDK_PROCESS_FORM_BINDING` (

  `ID` BIGINT NOT NULL COMMENT '主键',
  `FORM_ID` BIGINT NOT NULL COMMENT '表单ID',
  `PROCESS_DEFINITION_ID` VARCHAR(128) COMMENT 'Flowable流程定义ID',
  `PROCESS_DEFINITION_KEY` VARCHAR(128) NOT NULL COMMENT 'Flowable流程定义KEY',
  `PROCESS_DEFINITION_VERSION` INT COMMENT 'Flowable流程定义版本',
  `DEPLOYMENT_ID` VARCHAR(128) COMMENT 'Flowable部署ID',
  `TENANT_ID` VARCHAR(128) DEFAULT '' NOT NULL COMMENT '租户ID',
  `BIND_SCOPE` VARCHAR(32) DEFAULT 'PROCESS' NOT NULL COMMENT '绑定范围：PROCESS流程，START发起，TASK任务',
  `TASK_DEFINITION_KEY` VARCHAR(128) DEFAULT '' NOT NULL COMMENT '任务定义KEY，流程级绑定为空字符串',
  `STATUS` INT DEFAULT 1 NOT NULL COMMENT '状态：1启用，0停用',
  `REMARK` VARCHAR(500) COMMENT '备注',
  `CREATE_TIME` DATETIME(6) COMMENT '创建时间',
  `UPDATE_TIME` DATETIME(6) COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='流程表单绑定表';
-- ----------------------------
-- Records of WCDK_PROCESS_FORM_BINDING
-- ----------------------------

-- ----------------------------
-- Table structure for WCDK_PROCESS_REQUEST
-- ----------------------------
DROP TABLE IF EXISTS `WCDK_PROCESS_REQUEST`;
CREATE TABLE `WCDK_PROCESS_REQUEST` (

  `ID` BIGINT NOT NULL COMMENT '主键',
  `PROCESS_NO` VARCHAR(64) NOT NULL COMMENT '流程申请编号',
  `STARTER` VARCHAR(64) NOT NULL COMMENT '流程发起人',
  `TASK_NAME` VARCHAR(255) NOT NULL COMMENT '任务名称',
  `BUSINESS_TITLE` VARCHAR(1000) NOT NULL COMMENT '业务标题',
  `FORM_DATA_JSON` LONGTEXT COMMENT '动态表单数据JSON',
  `STATUS` VARCHAR(32) NOT NULL COMMENT '流程申请状态',
  `PROCESS_INSTANCE_ID` VARCHAR(64) COMMENT '流程实例ID',
  `CURRENT_TASK_ID` VARCHAR(64) COMMENT '当前任务ID',
  `CURRENT_TASK_NAME` VARCHAR(128) COMMENT '当前任务名称',
  `PROCESS_DEFINITION_KEY` VARCHAR(128) NOT NULL COMMENT '流程定义标识',
  `CREATE_TIME` DATETIME(6) COMMENT '创建时间',
  `UPDATE_TIME` DATETIME(6) COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='流程申请业务表';
-- ----------------------------
-- Records of WCDK_PROCESS_REQUEST
-- ----------------------------

-- ----------------------------
-- Primary Key structure for table ACT_EVT_LOG
-- ----------------------------
ALTER TABLE `ACT_EVT_LOG` ADD PRIMARY KEY (`LOG_NR_`);

-- ----------------------------
-- Primary Key structure for table ACT_GE_BYTEARRAY
-- ----------------------------
ALTER TABLE `ACT_GE_BYTEARRAY` ADD PRIMARY KEY (`ID_`);

-- ----------------------------
-- Checks structure for table ACT_GE_BYTEARRAY
-- ----------------------------
ALTER TABLE `ACT_GE_BYTEARRAY` ADD CHECK (GENERATED_ IN (1, 0));

-- ----------------------------
-- Indexes structure for table ACT_GE_BYTEARRAY
-- ----------------------------
CREATE INDEX `ACT_IDX_BYTEAR_DEPL`
  ON `ACT_GE_BYTEARRAY` (`DEPLOYMENT_ID_`);

-- ----------------------------
-- Primary Key structure for table ACT_GE_PROPERTY
-- ----------------------------
ALTER TABLE `ACT_GE_PROPERTY` ADD PRIMARY KEY (`NAME_`);

-- ----------------------------
-- Primary Key structure for table ACT_HI_ACTINST
-- ----------------------------
ALTER TABLE `ACT_HI_ACTINST` ADD PRIMARY KEY (`ID_`);

-- ----------------------------
-- Indexes structure for table ACT_HI_ACTINST
-- ----------------------------
CREATE INDEX `ACT_IDX_HI_ACT_INST_END`
  ON `ACT_HI_ACTINST` (`END_TIME_`);
CREATE INDEX `ACT_IDX_HI_ACT_INST_EXEC`
  ON `ACT_HI_ACTINST` (`EXECUTION_ID_`, `ACT_ID_`);
CREATE INDEX `ACT_IDX_HI_ACT_INST_PROCINST`
  ON `ACT_HI_ACTINST` (`PROC_INST_ID_`, `ACT_ID_`);
CREATE INDEX `ACT_IDX_HI_ACT_INST_START`
  ON `ACT_HI_ACTINST` (`START_TIME_`);

-- ----------------------------
-- Primary Key structure for table ACT_HI_ATTACHMENT
-- ----------------------------
ALTER TABLE `ACT_HI_ATTACHMENT` ADD PRIMARY KEY (`ID_`);

-- ----------------------------
-- Primary Key structure for table ACT_HI_COMMENT
-- ----------------------------
ALTER TABLE `ACT_HI_COMMENT` ADD PRIMARY KEY (`ID_`);

-- ----------------------------
-- Primary Key structure for table ACT_HI_DETAIL
-- ----------------------------
ALTER TABLE `ACT_HI_DETAIL` ADD PRIMARY KEY (`ID_`);

-- ----------------------------
-- Indexes structure for table ACT_HI_DETAIL
-- ----------------------------
CREATE INDEX `ACT_IDX_HI_DETAIL_ACT_INST`
  ON `ACT_HI_DETAIL` (`ACT_INST_ID_`);
CREATE INDEX `ACT_IDX_HI_DETAIL_NAME`
  ON `ACT_HI_DETAIL` (`NAME_`);
CREATE INDEX `ACT_IDX_HI_DETAIL_PROC_INST`
  ON `ACT_HI_DETAIL` (`PROC_INST_ID_`);
CREATE INDEX `ACT_IDX_HI_DETAIL_TASK_ID`
  ON `ACT_HI_DETAIL` (`TASK_ID_`);
CREATE INDEX `ACT_IDX_HI_DETAIL_TIME`
  ON `ACT_HI_DETAIL` (`TIME_`);

-- ----------------------------
-- Primary Key structure for table ACT_HI_ENTITYLINK
-- ----------------------------
ALTER TABLE `ACT_HI_ENTITYLINK` ADD PRIMARY KEY (`ID_`);

-- ----------------------------
-- Indexes structure for table ACT_HI_ENTITYLINK
-- ----------------------------
CREATE INDEX `ACT_IDX_HI_ENT_LNK_REF_SCOPE`
  ON `ACT_HI_ENTITYLINK` (`REF_SCOPE_ID_`, `REF_SCOPE_TYPE_`, `LINK_TYPE_`);
CREATE INDEX `ACT_IDX_HI_ENT_LNK_ROOT_SCOPE`
  ON `ACT_HI_ENTITYLINK` (`ROOT_SCOPE_ID_`, `ROOT_SCOPE_TYPE_`, `LINK_TYPE_`);
CREATE INDEX `ACT_IDX_HI_ENT_LNK_SCOPE`
  ON `ACT_HI_ENTITYLINK` (`SCOPE_ID_`, `SCOPE_TYPE_`, `LINK_TYPE_`);
CREATE INDEX `ACT_IDX_HI_ENT_LNK_SCOPE_DEF`
  ON `ACT_HI_ENTITYLINK` (`SCOPE_DEFINITION_ID_`, `SCOPE_TYPE_`, `LINK_TYPE_`);

-- ----------------------------
-- Primary Key structure for table ACT_HI_IDENTITYLINK
-- ----------------------------
ALTER TABLE `ACT_HI_IDENTITYLINK` ADD PRIMARY KEY (`ID_`);

-- ----------------------------
-- Indexes structure for table ACT_HI_IDENTITYLINK
-- ----------------------------
CREATE INDEX `ACT_IDX_HI_IDENT_LNK_PROCINST`
  ON `ACT_HI_IDENTITYLINK` (`PROC_INST_ID_`);
CREATE INDEX `ACT_IDX_HI_IDENT_LNK_SCOPE`
  ON `ACT_HI_IDENTITYLINK` (`SCOPE_ID_`, `SCOPE_TYPE_`);
CREATE INDEX `ACT_IDX_HI_IDENT_LNK_SCOPE_DEF`
  ON `ACT_HI_IDENTITYLINK` (`SCOPE_DEFINITION_ID_`, `SCOPE_TYPE_`);
CREATE INDEX `ACT_IDX_HI_IDENT_LNK_SUB_SCOPE`
  ON `ACT_HI_IDENTITYLINK` (`SUB_SCOPE_ID_`, `SCOPE_TYPE_`);
CREATE INDEX `ACT_IDX_HI_IDENT_LNK_TASK`
  ON `ACT_HI_IDENTITYLINK` (`TASK_ID_`);
CREATE INDEX `ACT_IDX_HI_IDENT_LNK_USER`
  ON `ACT_HI_IDENTITYLINK` (`USER_ID_`);

-- ----------------------------
-- Primary Key structure for table ACT_HI_PROCINST
-- ----------------------------
ALTER TABLE `ACT_HI_PROCINST` ADD PRIMARY KEY (`ID_`);

-- ----------------------------
-- Uniques structure for table ACT_HI_PROCINST
-- ----------------------------
ALTER TABLE `ACT_HI_PROCINST` ADD UNIQUE (`PROC_INST_ID_`);

-- ----------------------------
-- Indexes structure for table ACT_HI_PROCINST
-- ----------------------------
CREATE INDEX `ACT_IDX_HI_PRO_I_BUSKEY`
  ON `ACT_HI_PROCINST` (`BUSINESS_KEY_`);
CREATE INDEX `ACT_IDX_HI_PRO_INST_END`
  ON `ACT_HI_PROCINST` (`END_TIME_`);
CREATE INDEX `ACT_IDX_HI_PRO_SUPER_PROCINST`
  ON `ACT_HI_PROCINST` (`SUPER_PROCESS_INSTANCE_ID_`);

-- ----------------------------
-- Primary Key structure for table ACT_HI_TASKINST
-- ----------------------------
ALTER TABLE `ACT_HI_TASKINST` ADD PRIMARY KEY (`ID_`);

-- ----------------------------
-- Indexes structure for table ACT_HI_TASKINST
-- ----------------------------
CREATE INDEX `ACT_IDX_HI_TASK_INST_PROCINST`
  ON `ACT_HI_TASKINST` (`PROC_INST_ID_`);
CREATE INDEX `ACT_IDX_HI_TASK_SCOPE`
  ON `ACT_HI_TASKINST` (`SCOPE_ID_`, `SCOPE_TYPE_`);
CREATE INDEX `ACT_IDX_HI_TASK_SCOPE_DEF`
  ON `ACT_HI_TASKINST` (`SCOPE_DEFINITION_ID_`, `SCOPE_TYPE_`);
CREATE INDEX `ACT_IDX_HI_TASK_SUB_SCOPE`
  ON `ACT_HI_TASKINST` (`SUB_SCOPE_ID_`, `SCOPE_TYPE_`);

-- ----------------------------
-- Primary Key structure for table ACT_HI_TSK_LOG
-- ----------------------------
ALTER TABLE `ACT_HI_TSK_LOG` ADD PRIMARY KEY (`ID_`);

-- ----------------------------
-- Indexes structure for table ACT_HI_TSK_LOG
-- ----------------------------
CREATE INDEX `ACT_IDX_ACT_HI_TSK_LOG_TASK`
  ON `ACT_HI_TSK_LOG` (`TASK_ID_`);

-- ----------------------------
-- Primary Key structure for table ACT_HI_VARINST
-- ----------------------------
ALTER TABLE `ACT_HI_VARINST` ADD PRIMARY KEY (`ID_`);

-- ----------------------------
-- Indexes structure for table ACT_HI_VARINST
-- ----------------------------
CREATE INDEX `ACT_IDX_HI_PROCVAR_EXE`
  ON `ACT_HI_VARINST` (`EXECUTION_ID_`);
CREATE INDEX `ACT_IDX_HI_PROCVAR_NAME_TYPE`
  ON `ACT_HI_VARINST` (`NAME_`, `VAR_TYPE_`);
CREATE INDEX `ACT_IDX_HI_PROCVAR_PROC_INST`
  ON `ACT_HI_VARINST` (`PROC_INST_ID_`);
CREATE INDEX `ACT_IDX_HI_PROCVAR_TASK_ID`
  ON `ACT_HI_VARINST` (`TASK_ID_`);
CREATE INDEX `ACT_IDX_HI_VAR_SCOPE_ID_TYPE`
  ON `ACT_HI_VARINST` (`SCOPE_ID_`, `SCOPE_TYPE_`);
CREATE INDEX `ACT_IDX_HI_VAR_SUB_ID_TYPE`
  ON `ACT_HI_VARINST` (`SUB_SCOPE_ID_`, `SCOPE_TYPE_`);

-- ----------------------------
-- Primary Key structure for table ACT_ID_BYTEARRAY
-- ----------------------------
ALTER TABLE `ACT_ID_BYTEARRAY` ADD PRIMARY KEY (`ID_`);

-- ----------------------------
-- Primary Key structure for table ACT_ID_GROUP
-- ----------------------------
ALTER TABLE `ACT_ID_GROUP` ADD PRIMARY KEY (`ID_`);

-- ----------------------------
-- Primary Key structure for table ACT_ID_INFO
-- ----------------------------
ALTER TABLE `ACT_ID_INFO` ADD PRIMARY KEY (`ID_`);

-- ----------------------------
-- Primary Key structure for table ACT_ID_MEMBERSHIP
-- ----------------------------
ALTER TABLE `ACT_ID_MEMBERSHIP` ADD PRIMARY KEY (`USER_ID_`, `GROUP_ID_`);

-- ----------------------------
-- Indexes structure for table ACT_ID_MEMBERSHIP
-- ----------------------------
CREATE INDEX `ACT_IDX_MEMB_GROUP`
  ON `ACT_ID_MEMBERSHIP` (`GROUP_ID_`);
CREATE INDEX `ACT_IDX_MEMB_USER`
  ON `ACT_ID_MEMBERSHIP` (`USER_ID_`);

-- ----------------------------
-- Primary Key structure for table ACT_ID_PRIV
-- ----------------------------
ALTER TABLE `ACT_ID_PRIV` ADD PRIMARY KEY (`ID_`);

-- ----------------------------
-- Uniques structure for table ACT_ID_PRIV
-- ----------------------------
ALTER TABLE `ACT_ID_PRIV` ADD UNIQUE (`NAME_`);

-- ----------------------------
-- Primary Key structure for table ACT_ID_PRIV_MAPPING
-- ----------------------------
ALTER TABLE `ACT_ID_PRIV_MAPPING` ADD PRIMARY KEY (`ID_`);

-- ----------------------------
-- Indexes structure for table ACT_ID_PRIV_MAPPING
-- ----------------------------
CREATE INDEX `ACT_IDX_PRIV_GROUP`
  ON `ACT_ID_PRIV_MAPPING` (`GROUP_ID_`);
CREATE INDEX `ACT_IDX_PRIV_MAPPING`
  ON `ACT_ID_PRIV_MAPPING` (`PRIV_ID_`);
CREATE INDEX `ACT_IDX_PRIV_USER`
  ON `ACT_ID_PRIV_MAPPING` (`USER_ID_`);

-- ----------------------------
-- Primary Key structure for table ACT_ID_PROPERTY
-- ----------------------------
ALTER TABLE `ACT_ID_PROPERTY` ADD PRIMARY KEY (`NAME_`);

-- ----------------------------
-- Primary Key structure for table ACT_ID_TOKEN
-- ----------------------------
ALTER TABLE `ACT_ID_TOKEN` ADD PRIMARY KEY (`ID_`);

-- ----------------------------
-- Primary Key structure for table ACT_ID_USER
-- ----------------------------
ALTER TABLE `ACT_ID_USER` ADD PRIMARY KEY (`ID_`);

-- ----------------------------
-- Primary Key structure for table ACT_PROCDEF_INFO
-- ----------------------------
ALTER TABLE `ACT_PROCDEF_INFO` ADD PRIMARY KEY (`ID_`);

-- ----------------------------
-- Uniques structure for table ACT_PROCDEF_INFO
-- ----------------------------
ALTER TABLE `ACT_PROCDEF_INFO` ADD UNIQUE (`PROC_DEF_ID_`);

-- ----------------------------
-- Indexes structure for table ACT_PROCDEF_INFO
-- ----------------------------
CREATE INDEX `ACT_IDX_PROCDEF_INFO_JSON`
  ON `ACT_PROCDEF_INFO` (`INFO_JSON_ID_`);
CREATE INDEX `ACT_IDX_PROCDEF_INFO_PROC`
  ON `ACT_PROCDEF_INFO` (`PROC_DEF_ID_`);

-- ----------------------------
-- Primary Key structure for table ACT_RE_DEPLOYMENT
-- ----------------------------
ALTER TABLE `ACT_RE_DEPLOYMENT` ADD PRIMARY KEY (`ID_`);

-- ----------------------------
-- Primary Key structure for table ACT_RE_MODEL
-- ----------------------------
ALTER TABLE `ACT_RE_MODEL` ADD PRIMARY KEY (`ID_`);

-- ----------------------------
-- Indexes structure for table ACT_RE_MODEL
-- ----------------------------
CREATE INDEX `ACT_IDX_MODEL_DEPLOYMENT`
  ON `ACT_RE_MODEL` (`DEPLOYMENT_ID_`);
CREATE INDEX `ACT_IDX_MODEL_SOURCE`
  ON `ACT_RE_MODEL` (`EDITOR_SOURCE_VALUE_ID_`);
CREATE INDEX `ACT_IDX_MODEL_SOURCE_EXTRA`
  ON `ACT_RE_MODEL` (`EDITOR_SOURCE_EXTRA_VALUE_ID_`);

-- ----------------------------
-- Primary Key structure for table ACT_RE_PROCDEF
-- ----------------------------
ALTER TABLE `ACT_RE_PROCDEF` ADD PRIMARY KEY (`ID_`);

-- ----------------------------
-- Uniques structure for table ACT_RE_PROCDEF
-- ----------------------------
ALTER TABLE `ACT_RE_PROCDEF` ADD UNIQUE (`KEY_`, `VERSION_`, `DERIVED_VERSION_`, `TENANT_ID_`);

-- ----------------------------
-- Checks structure for table ACT_RE_PROCDEF
-- ----------------------------
ALTER TABLE `ACT_RE_PROCDEF` ADD CHECK (HAS_START_FORM_KEY_ IN (1, 0));
ALTER TABLE `ACT_RE_PROCDEF` ADD CHECK (HAS_GRAPHICAL_NOTATION_ IN (1, 0));

-- ----------------------------
-- Primary Key structure for table ACT_RU_ACTINST
-- ----------------------------
ALTER TABLE `ACT_RU_ACTINST` ADD PRIMARY KEY (`ID_`);

-- ----------------------------
-- Indexes structure for table ACT_RU_ACTINST
-- ----------------------------
CREATE INDEX `ACT_IDX_RU_ACTI_END`
  ON `ACT_RU_ACTINST` (`END_TIME_`);
CREATE INDEX `ACT_IDX_RU_ACTI_EXEC`
  ON `ACT_RU_ACTINST` (`EXECUTION_ID_`);
CREATE INDEX `ACT_IDX_RU_ACTI_EXEC_ACT`
  ON `ACT_RU_ACTINST` (`EXECUTION_ID_`, `ACT_ID_`);
CREATE INDEX `ACT_IDX_RU_ACTI_PROC`
  ON `ACT_RU_ACTINST` (`PROC_INST_ID_`);
CREATE INDEX `ACT_IDX_RU_ACTI_PROC_ACT`
  ON `ACT_RU_ACTINST` (`PROC_INST_ID_`, `ACT_ID_`);
CREATE INDEX `ACT_IDX_RU_ACTI_START`
  ON `ACT_RU_ACTINST` (`START_TIME_`);
CREATE INDEX `ACT_IDX_RU_ACTI_TASK`
  ON `ACT_RU_ACTINST` (`TASK_ID_`);

-- ----------------------------
-- Primary Key structure for table ACT_RU_DEADLETTER_JOB
-- ----------------------------
ALTER TABLE `ACT_RU_DEADLETTER_JOB` ADD PRIMARY KEY (`ID_`);

-- ----------------------------
-- Checks structure for table ACT_RU_DEADLETTER_JOB
-- ----------------------------
ALTER TABLE `ACT_RU_DEADLETTER_JOB` ADD CHECK (EXCLUSIVE_ IN (1, 0));

-- ----------------------------
-- Indexes structure for table ACT_RU_DEADLETTER_JOB
-- ----------------------------
CREATE INDEX `ACT_IDX_DJOB_CORRELATION_ID`
  ON `ACT_RU_DEADLETTER_JOB` (`CORRELATION_ID_`);
CREATE INDEX `ACT_IDX_DJOB_CUSTOM_VAL_ID`
  ON `ACT_RU_DEADLETTER_JOB` (`CUSTOM_VALUES_ID_`);
CREATE INDEX `ACT_IDX_DJOB_EXCEPTION`
  ON `ACT_RU_DEADLETTER_JOB` (`EXCEPTION_STACK_ID_`);
CREATE INDEX `ACT_IDX_DJOB_EXECUTION_ID`
  ON `ACT_RU_DEADLETTER_JOB` (`EXECUTION_ID_`);
CREATE INDEX `ACT_IDX_DJOB_PROC_DEF_ID`
  ON `ACT_RU_DEADLETTER_JOB` (`PROC_DEF_ID_`);
CREATE INDEX `ACT_IDX_DJOB_PROC_INST_ID`
  ON `ACT_RU_DEADLETTER_JOB` (`PROCESS_INSTANCE_ID_`);
CREATE INDEX `ACT_IDX_DJOB_SCOPE`
  ON `ACT_RU_DEADLETTER_JOB` (`SCOPE_ID_`, `SCOPE_TYPE_`);
CREATE INDEX `ACT_IDX_DJOB_SCOPE_DEF`
  ON `ACT_RU_DEADLETTER_JOB` (`SCOPE_DEFINITION_ID_`, `SCOPE_TYPE_`);
CREATE INDEX `ACT_IDX_DJOB_SUB_SCOPE`
  ON `ACT_RU_DEADLETTER_JOB` (`SUB_SCOPE_ID_`, `SCOPE_TYPE_`);

-- ----------------------------
-- Primary Key structure for table ACT_RU_ENTITYLINK
-- ----------------------------
ALTER TABLE `ACT_RU_ENTITYLINK` ADD PRIMARY KEY (`ID_`);

-- ----------------------------
-- Indexes structure for table ACT_RU_ENTITYLINK
-- ----------------------------
CREATE INDEX `ACT_IDX_ENT_LNK_REF_SCOPE`
  ON `ACT_RU_ENTITYLINK` (`REF_SCOPE_ID_`, `REF_SCOPE_TYPE_`, `LINK_TYPE_`);
CREATE INDEX `ACT_IDX_ENT_LNK_ROOT_SCOPE`
  ON `ACT_RU_ENTITYLINK` (`ROOT_SCOPE_ID_`, `ROOT_SCOPE_TYPE_`, `LINK_TYPE_`);
CREATE INDEX `ACT_IDX_ENT_LNK_SCOPE`
  ON `ACT_RU_ENTITYLINK` (`SCOPE_ID_`, `SCOPE_TYPE_`, `LINK_TYPE_`);
CREATE INDEX `ACT_IDX_ENT_LNK_SCOPE_DEF`
  ON `ACT_RU_ENTITYLINK` (`SCOPE_DEFINITION_ID_`, `SCOPE_TYPE_`, `LINK_TYPE_`);

-- ----------------------------
-- Primary Key structure for table ACT_RU_EVENT_SUBSCR
-- ----------------------------
ALTER TABLE `ACT_RU_EVENT_SUBSCR` ADD PRIMARY KEY (`ID_`);

-- ----------------------------
-- Indexes structure for table ACT_RU_EVENT_SUBSCR
-- ----------------------------
CREATE INDEX `ACT_IDX_EVENT_SUBSCR`
  ON `ACT_RU_EVENT_SUBSCR` (`EXECUTION_ID_`);
CREATE INDEX `ACT_IDX_EVENT_SUBSCR_CONFIG_`
  ON `ACT_RU_EVENT_SUBSCR` (`CONFIGURATION_`);
CREATE INDEX `ACT_IDX_EVENT_SUBSCR_PROC_ID`
  ON `ACT_RU_EVENT_SUBSCR` (`PROC_INST_ID_`);
CREATE INDEX `ACT_IDX_EVENT_SUBSCR_SCOPEREF_`
  ON `ACT_RU_EVENT_SUBSCR` (`SCOPE_ID_`, `SCOPE_TYPE_`);

-- ----------------------------
-- Primary Key structure for table ACT_RU_EXECUTION
-- ----------------------------
ALTER TABLE `ACT_RU_EXECUTION` ADD PRIMARY KEY (`ID_`);

-- ----------------------------
-- Checks structure for table ACT_RU_EXECUTION
-- ----------------------------
ALTER TABLE `ACT_RU_EXECUTION` ADD CHECK (IS_ACTIVE_ IN (1, 0));
ALTER TABLE `ACT_RU_EXECUTION` ADD CHECK (IS_CONCURRENT_ IN (1, 0));
ALTER TABLE `ACT_RU_EXECUTION` ADD CHECK (IS_SCOPE_ IN (1, 0));
ALTER TABLE `ACT_RU_EXECUTION` ADD CHECK (IS_EVENT_SCOPE_ IN (1, 0));
ALTER TABLE `ACT_RU_EXECUTION` ADD CHECK (IS_MI_ROOT_ IN (1, 0));
ALTER TABLE `ACT_RU_EXECUTION` ADD CHECK (IS_COUNT_ENABLED_ IN (1, 0));

-- ----------------------------
-- Indexes structure for table ACT_RU_EXECUTION
-- ----------------------------
CREATE INDEX `ACT_IDX_EXE_PARENT`
  ON `ACT_RU_EXECUTION` (`PARENT_ID_`);
CREATE INDEX `ACT_IDX_EXE_PROCDEF`
  ON `ACT_RU_EXECUTION` (`PROC_DEF_ID_`);
CREATE INDEX `ACT_IDX_EXE_PROCINST`
  ON `ACT_RU_EXECUTION` (`PROC_INST_ID_`);
CREATE INDEX `ACT_IDX_EXE_SUPER`
  ON `ACT_RU_EXECUTION` (`SUPER_EXEC_`);
CREATE INDEX `ACT_IDX_EXEC_BUSKEY`
  ON `ACT_RU_EXECUTION` (`BUSINESS_KEY_`);
CREATE INDEX `ACT_IDX_EXEC_REF_ID_`
  ON `ACT_RU_EXECUTION` (`REFERENCE_ID_`);
CREATE INDEX `ACT_IDX_EXEC_ROOT`
  ON `ACT_RU_EXECUTION` (`ROOT_PROC_INST_ID_`);

-- ----------------------------
-- Primary Key structure for table ACT_RU_EXTERNAL_JOB
-- ----------------------------
ALTER TABLE `ACT_RU_EXTERNAL_JOB` ADD PRIMARY KEY (`ID_`);

-- ----------------------------
-- Checks structure for table ACT_RU_EXTERNAL_JOB
-- ----------------------------
ALTER TABLE `ACT_RU_EXTERNAL_JOB` ADD CHECK (EXCLUSIVE_ IN (1, 0));

-- ----------------------------
-- Indexes structure for table ACT_RU_EXTERNAL_JOB
-- ----------------------------
CREATE INDEX `ACT_IDX_EJOB_CORRELATION_ID`
  ON `ACT_RU_EXTERNAL_JOB` (`CORRELATION_ID_`);
CREATE INDEX `ACT_IDX_EJOB_CUSTOM_VAL_ID`
  ON `ACT_RU_EXTERNAL_JOB` (`CUSTOM_VALUES_ID_`);
CREATE INDEX `ACT_IDX_EJOB_EXCEPTION`
  ON `ACT_RU_EXTERNAL_JOB` (`EXCEPTION_STACK_ID_`);
CREATE INDEX `ACT_IDX_EJOB_SCOPE`
  ON `ACT_RU_EXTERNAL_JOB` (`SCOPE_ID_`, `SCOPE_TYPE_`);
CREATE INDEX `ACT_IDX_EJOB_SCOPE_DEF`
  ON `ACT_RU_EXTERNAL_JOB` (`SCOPE_DEFINITION_ID_`, `SCOPE_TYPE_`);
CREATE INDEX `ACT_IDX_EJOB_SUB_SCOPE`
  ON `ACT_RU_EXTERNAL_JOB` (`SUB_SCOPE_ID_`, `SCOPE_TYPE_`);

-- ----------------------------
-- Primary Key structure for table ACT_RU_HISTORY_JOB
-- ----------------------------
ALTER TABLE `ACT_RU_HISTORY_JOB` ADD PRIMARY KEY (`ID_`);

-- ----------------------------
-- Primary Key structure for table ACT_RU_IDENTITYLINK
-- ----------------------------
ALTER TABLE `ACT_RU_IDENTITYLINK` ADD PRIMARY KEY (`ID_`);

-- ----------------------------
-- Indexes structure for table ACT_RU_IDENTITYLINK
-- ----------------------------
CREATE INDEX `ACT_IDX_ATHRZ_PROCEDEF`
  ON `ACT_RU_IDENTITYLINK` (`PROC_DEF_ID_`);
CREATE INDEX `ACT_IDX_IDENT_LNK_GROUP`
  ON `ACT_RU_IDENTITYLINK` (`GROUP_ID_`);
CREATE INDEX `ACT_IDX_IDENT_LNK_SCOPE`
  ON `ACT_RU_IDENTITYLINK` (`SCOPE_ID_`, `SCOPE_TYPE_`);
CREATE INDEX `ACT_IDX_IDENT_LNK_SCOPE_DEF`
  ON `ACT_RU_IDENTITYLINK` (`SCOPE_DEFINITION_ID_`, `SCOPE_TYPE_`);
CREATE INDEX `ACT_IDX_IDENT_LNK_SUB_SCOPE`
  ON `ACT_RU_IDENTITYLINK` (`SUB_SCOPE_ID_`, `SCOPE_TYPE_`);
CREATE INDEX `ACT_IDX_IDENT_LNK_USER`
  ON `ACT_RU_IDENTITYLINK` (`USER_ID_`);
CREATE INDEX `ACT_IDX_IDL_PROCINST`
  ON `ACT_RU_IDENTITYLINK` (`PROC_INST_ID_`);
CREATE INDEX `ACT_IDX_TSKASS_TASK`
  ON `ACT_RU_IDENTITYLINK` (`TASK_ID_`);

-- ----------------------------
-- Primary Key structure for table ACT_RU_JOB
-- ----------------------------
ALTER TABLE `ACT_RU_JOB` ADD PRIMARY KEY (`ID_`);

-- ----------------------------
-- Checks structure for table ACT_RU_JOB
-- ----------------------------
ALTER TABLE `ACT_RU_JOB` ADD CHECK (EXCLUSIVE_ IN (1, 0));

-- ----------------------------
-- Indexes structure for table ACT_RU_JOB
-- ----------------------------
CREATE INDEX `ACT_IDX_JOB_CORRELATION_ID`
  ON `ACT_RU_JOB` (`CORRELATION_ID_`);
CREATE INDEX `ACT_IDX_JOB_CUSTOM_VAL_ID`
  ON `ACT_RU_JOB` (`CUSTOM_VALUES_ID_`);
CREATE INDEX `ACT_IDX_JOB_EXCEPTION`
  ON `ACT_RU_JOB` (`EXCEPTION_STACK_ID_`);
CREATE INDEX `ACT_IDX_JOB_EXECUTION_ID`
  ON `ACT_RU_JOB` (`EXECUTION_ID_`);
CREATE INDEX `ACT_IDX_JOB_PROC_DEF_ID`
  ON `ACT_RU_JOB` (`PROC_DEF_ID_`);
CREATE INDEX `ACT_IDX_JOB_PROC_INST_ID`
  ON `ACT_RU_JOB` (`PROCESS_INSTANCE_ID_`);
CREATE INDEX `ACT_IDX_JOB_SCOPE`
  ON `ACT_RU_JOB` (`SCOPE_ID_`, `SCOPE_TYPE_`);
CREATE INDEX `ACT_IDX_JOB_SCOPE_DEF`
  ON `ACT_RU_JOB` (`SCOPE_DEFINITION_ID_`, `SCOPE_TYPE_`);
CREATE INDEX `ACT_IDX_JOB_SUB_SCOPE`
  ON `ACT_RU_JOB` (`SUB_SCOPE_ID_`, `SCOPE_TYPE_`);

-- ----------------------------
-- Primary Key structure for table ACT_RU_SUSPENDED_JOB
-- ----------------------------
ALTER TABLE `ACT_RU_SUSPENDED_JOB` ADD PRIMARY KEY (`ID_`);

-- ----------------------------
-- Checks structure for table ACT_RU_SUSPENDED_JOB
-- ----------------------------
ALTER TABLE `ACT_RU_SUSPENDED_JOB` ADD CHECK (EXCLUSIVE_ IN (1, 0));

-- ----------------------------
-- Indexes structure for table ACT_RU_SUSPENDED_JOB
-- ----------------------------
CREATE INDEX `ACT_IDX_SJOB_CORRELATION_ID`
  ON `ACT_RU_SUSPENDED_JOB` (`CORRELATION_ID_`);
CREATE INDEX `ACT_IDX_SJOB_CUSTOM_VAL_ID`
  ON `ACT_RU_SUSPENDED_JOB` (`CUSTOM_VALUES_ID_`);
CREATE INDEX `ACT_IDX_SJOB_EXCEPTION`
  ON `ACT_RU_SUSPENDED_JOB` (`EXCEPTION_STACK_ID_`);
CREATE INDEX `ACT_IDX_SJOB_EXECUTION_ID`
  ON `ACT_RU_SUSPENDED_JOB` (`EXECUTION_ID_`);
CREATE INDEX `ACT_IDX_SJOB_PROC_DEF_ID`
  ON `ACT_RU_SUSPENDED_JOB` (`PROC_DEF_ID_`);
CREATE INDEX `ACT_IDX_SJOB_PROC_INST_ID`
  ON `ACT_RU_SUSPENDED_JOB` (`PROCESS_INSTANCE_ID_`);
CREATE INDEX `ACT_IDX_SJOB_SCOPE`
  ON `ACT_RU_SUSPENDED_JOB` (`SCOPE_ID_`, `SCOPE_TYPE_`);
CREATE INDEX `ACT_IDX_SJOB_SCOPE_DEF`
  ON `ACT_RU_SUSPENDED_JOB` (`SCOPE_DEFINITION_ID_`, `SCOPE_TYPE_`);
CREATE INDEX `ACT_IDX_SJOB_SUB_SCOPE`
  ON `ACT_RU_SUSPENDED_JOB` (`SUB_SCOPE_ID_`, `SCOPE_TYPE_`);

-- ----------------------------
-- Primary Key structure for table ACT_RU_TASK
-- ----------------------------
ALTER TABLE `ACT_RU_TASK` ADD PRIMARY KEY (`ID_`);

-- ----------------------------
-- Checks structure for table ACT_RU_TASK
-- ----------------------------
ALTER TABLE `ACT_RU_TASK` ADD CHECK (IS_COUNT_ENABLED_ IN (1, 0));

-- ----------------------------
-- Indexes structure for table ACT_RU_TASK
-- ----------------------------
CREATE INDEX `ACT_IDX_TASK_CREATE`
  ON `ACT_RU_TASK` (`CREATE_TIME_`);
CREATE INDEX `ACT_IDX_TASK_EXEC`
  ON `ACT_RU_TASK` (`EXECUTION_ID_`);
CREATE INDEX `ACT_IDX_TASK_PROCDEF`
  ON `ACT_RU_TASK` (`PROC_DEF_ID_`);
CREATE INDEX `ACT_IDX_TASK_PROCINST`
  ON `ACT_RU_TASK` (`PROC_INST_ID_`);
CREATE INDEX `ACT_IDX_TASK_SCOPE`
  ON `ACT_RU_TASK` (`SCOPE_ID_`, `SCOPE_TYPE_`);
CREATE INDEX `ACT_IDX_TASK_SCOPE_DEF`
  ON `ACT_RU_TASK` (`SCOPE_DEFINITION_ID_`, `SCOPE_TYPE_`);
CREATE INDEX `ACT_IDX_TASK_SUB_SCOPE`
  ON `ACT_RU_TASK` (`SUB_SCOPE_ID_`, `SCOPE_TYPE_`);

-- ----------------------------
-- Primary Key structure for table ACT_RU_TIMER_JOB
-- ----------------------------
ALTER TABLE `ACT_RU_TIMER_JOB` ADD PRIMARY KEY (`ID_`);

-- ----------------------------
-- Checks structure for table ACT_RU_TIMER_JOB
-- ----------------------------
ALTER TABLE `ACT_RU_TIMER_JOB` ADD CHECK (EXCLUSIVE_ IN (1, 0));

-- ----------------------------
-- Indexes structure for table ACT_RU_TIMER_JOB
-- ----------------------------
CREATE INDEX `ACT_IDX_TJOB_CORRELATION_ID`
  ON `ACT_RU_TIMER_JOB` (`CORRELATION_ID_`);
CREATE INDEX `ACT_IDX_TJOB_CUSTOM_VAL_ID`
  ON `ACT_RU_TIMER_JOB` (`CUSTOM_VALUES_ID_`);
CREATE INDEX `ACT_IDX_TJOB_DUEDATE`
  ON `ACT_RU_TIMER_JOB` (`DUEDATE_`);
CREATE INDEX `ACT_IDX_TJOB_EXCEPTION`
  ON `ACT_RU_TIMER_JOB` (`EXCEPTION_STACK_ID_`);
CREATE INDEX `ACT_IDX_TJOB_EXECUTION_ID`
  ON `ACT_RU_TIMER_JOB` (`EXECUTION_ID_`);
CREATE INDEX `ACT_IDX_TJOB_PROC_DEF_ID`
  ON `ACT_RU_TIMER_JOB` (`PROC_DEF_ID_`);
CREATE INDEX `ACT_IDX_TJOB_PROC_INST_ID`
  ON `ACT_RU_TIMER_JOB` (`PROCESS_INSTANCE_ID_`);
CREATE INDEX `ACT_IDX_TJOB_SCOPE`
  ON `ACT_RU_TIMER_JOB` (`SCOPE_ID_`, `SCOPE_TYPE_`);
CREATE INDEX `ACT_IDX_TJOB_SCOPE_DEF`
  ON `ACT_RU_TIMER_JOB` (`SCOPE_DEFINITION_ID_`, `SCOPE_TYPE_`);
CREATE INDEX `ACT_IDX_TJOB_SUB_SCOPE`
  ON `ACT_RU_TIMER_JOB` (`SUB_SCOPE_ID_`, `SCOPE_TYPE_`);

-- ----------------------------
-- Primary Key structure for table ACT_RU_VARIABLE
-- ----------------------------
ALTER TABLE `ACT_RU_VARIABLE` ADD PRIMARY KEY (`ID_`);

-- ----------------------------
-- Indexes structure for table ACT_RU_VARIABLE
-- ----------------------------
CREATE INDEX `ACT_IDX_RU_VAR_SCOPE_ID_TYPE`
  ON `ACT_RU_VARIABLE` (`SCOPE_ID_`, `SCOPE_TYPE_`);
CREATE INDEX `ACT_IDX_RU_VAR_SUB_ID_TYPE`
  ON `ACT_RU_VARIABLE` (`SUB_SCOPE_ID_`, `SCOPE_TYPE_`);
CREATE INDEX `ACT_IDX_VAR_BYTEARRAY`
  ON `ACT_RU_VARIABLE` (`BYTEARRAY_ID_`);
CREATE INDEX `ACT_IDX_VAR_EXE`
  ON `ACT_RU_VARIABLE` (`EXECUTION_ID_`);
CREATE INDEX `ACT_IDX_VAR_PROCINST`
  ON `ACT_RU_VARIABLE` (`PROC_INST_ID_`);
CREATE INDEX `ACT_IDX_VARIABLE_TASK_ID`
  ON `ACT_RU_VARIABLE` (`TASK_ID_`);

-- ----------------------------
-- Primary Key structure for table FLW_CHANNEL_DEFINITION
-- ----------------------------
ALTER TABLE `FLW_CHANNEL_DEFINITION` ADD PRIMARY KEY (`ID_`);

-- ----------------------------
-- Indexes structure for table FLW_CHANNEL_DEFINITION
-- ----------------------------
CREATE UNIQUE INDEX `ACT_IDX_CHANNEL_DEF_UNIQ`
  ON `FLW_CHANNEL_DEFINITION` (`KEY_`, `VERSION_`, `TENANT_ID_`);

-- ----------------------------
-- Primary Key structure for table FLW_EVENT_DEFINITION
-- ----------------------------
ALTER TABLE `FLW_EVENT_DEFINITION` ADD PRIMARY KEY (`ID_`);

-- ----------------------------
-- Indexes structure for table FLW_EVENT_DEFINITION
-- ----------------------------
CREATE UNIQUE INDEX `ACT_IDX_EVENT_DEF_UNIQ`
  ON `FLW_EVENT_DEFINITION` (`KEY_`, `VERSION_`, `TENANT_ID_`);

-- ----------------------------
-- Primary Key structure for table FLW_EVENT_DEPLOYMENT
-- ----------------------------
ALTER TABLE `FLW_EVENT_DEPLOYMENT` ADD PRIMARY KEY (`ID_`);

-- ----------------------------
-- Primary Key structure for table FLW_EVENT_RESOURCE
-- ----------------------------
ALTER TABLE `FLW_EVENT_RESOURCE` ADD PRIMARY KEY (`ID_`);

-- ----------------------------
-- Indexes structure for table FLW_EVENT_RESOURCE
-- ----------------------------
CREATE INDEX `FLW_IDX_EVENT_RSRC_DPL`
  ON `FLW_EVENT_RESOURCE` (`DEPLOYMENT_ID_`);

-- ----------------------------
-- Primary Key structure for table FLW_RU_BATCH
-- ----------------------------
ALTER TABLE `FLW_RU_BATCH` ADD PRIMARY KEY (`ID_`);

-- ----------------------------
-- Primary Key structure for table FLW_RU_BATCH_PART
-- ----------------------------
ALTER TABLE `FLW_RU_BATCH_PART` ADD PRIMARY KEY (`ID_`);

-- ----------------------------
-- Indexes structure for table FLW_RU_BATCH_PART
-- ----------------------------
CREATE INDEX `FLW_IDX_BATCH_PART`
  ON `FLW_RU_BATCH_PART` (`BATCH_ID_`);

-- ----------------------------
-- Primary Key structure for table SYS_DEPT
-- ----------------------------
ALTER TABLE `SYS_DEPT` ADD PRIMARY KEY (`ID`);

-- ----------------------------
-- Indexes structure for table SYS_DEPT
-- ----------------------------
CREATE UNIQUE INDEX `UK_SYS_DEPT_CODE`
  ON `SYS_DEPT` (`DEPT_CODE`);

-- ----------------------------
-- Primary Key structure for table SYS_PERMISSION
-- ----------------------------
ALTER TABLE `SYS_PERMISSION` ADD PRIMARY KEY (`ID`);

-- ----------------------------
-- Indexes structure for table SYS_PERMISSION
-- ----------------------------
CREATE UNIQUE INDEX `UK_SYS_PERMISSION_CODE`
  ON `SYS_PERMISSION` (`PERMISSION_CODE`);

-- ----------------------------
-- Primary Key structure for table SYS_ROLE
-- ----------------------------
ALTER TABLE `SYS_ROLE` ADD PRIMARY KEY (`ID`);

-- ----------------------------
-- Indexes structure for table SYS_ROLE
-- ----------------------------
CREATE UNIQUE INDEX `UK_SYS_ROLE_CODE`
  ON `SYS_ROLE` (`ROLE_CODE`);

-- ----------------------------
-- Primary Key structure for table SYS_ROLE_PERMISSION
-- ----------------------------
ALTER TABLE `SYS_ROLE_PERMISSION` ADD PRIMARY KEY (`ROLE_ID`, `PERMISSION_ID`);

-- ----------------------------
-- Primary Key structure for table SYS_USER
-- ----------------------------
ALTER TABLE `SYS_USER` ADD PRIMARY KEY (`ID`);

-- ----------------------------
-- Indexes structure for table SYS_USER
-- ----------------------------
CREATE UNIQUE INDEX `UK_SYS_USER_USERNAME`
  ON `SYS_USER` (`USERNAME`);

-- ----------------------------
-- Primary Key structure for table SYS_USER_ROLE
-- ----------------------------
ALTER TABLE `SYS_USER_ROLE` ADD PRIMARY KEY (`USER_ID`, `ROLE_ID`);

-- ----------------------------
-- Primary Key structure for table WCDK_PROCESS_CLIENT
-- ----------------------------
ALTER TABLE `WCDK_PROCESS_CLIENT` ADD PRIMARY KEY (`ID`);

-- ----------------------------
-- Primary Key structure for table WCDK_PROCESS_CLIENT_PROCESS
-- ----------------------------
ALTER TABLE `WCDK_PROCESS_CLIENT_PROCESS` ADD PRIMARY KEY (`ID`);

-- ----------------------------
-- Primary Key structure for table WCDK_PROCESS_DEFINITION_META
-- ----------------------------
ALTER TABLE `WCDK_PROCESS_DEFINITION_META` ADD PRIMARY KEY (`ID`);

-- ----------------------------
-- Indexes structure for table WCDK_PROCESS_DEFINITION_META
-- ----------------------------
CREATE INDEX `IDX_WCDK_PROCESS_DEFINITION_META_DEPLOY`
  ON `WCDK_PROCESS_DEFINITION_META` (`DEPLOYMENT_ID`);
CREATE INDEX `IDX_WCDK_PROCESS_DEFINITION_META_INVALID`
  ON `WCDK_PROCESS_DEFINITION_META` (`INVALID_STATUS`);
CREATE INDEX `IDX_WCDK_PROCESS_DEFINITION_META_KEY`
  ON `WCDK_PROCESS_DEFINITION_META` (`PROCESS_DEFINITION_KEY`);
CREATE UNIQUE INDEX `UK_WCDK_PROCESS_DEFINITION_META_DEF`
  ON `WCDK_PROCESS_DEFINITION_META` (`PROCESS_DEFINITION_ID`);

-- ----------------------------
-- Primary Key structure for table WCDK_PROCESS_FORM
-- ----------------------------
ALTER TABLE `WCDK_PROCESS_FORM` ADD PRIMARY KEY (`ID`);

-- ----------------------------
-- Indexes structure for table WCDK_PROCESS_FORM
-- ----------------------------
CREATE INDEX `IDX_WCDK_PROCESS_FORM_KEY`
  ON `WCDK_PROCESS_FORM` (`FORM_KEY`);
CREATE INDEX `IDX_WCDK_PROCESS_FORM_STATUS`
  ON `WCDK_PROCESS_FORM` (`STATUS`);
CREATE UNIQUE INDEX `UK_WCDK_PROCESS_FORM_KEY_VER`
  ON `WCDK_PROCESS_FORM` (`FORM_KEY`, `FORM_VERSION`, `TENANT_ID`);

-- ----------------------------
-- Primary Key structure for table WCDK_PROCESS_FORM_BINDING
-- ----------------------------
ALTER TABLE `WCDK_PROCESS_FORM_BINDING` ADD PRIMARY KEY (`ID`);

-- ----------------------------
-- Indexes structure for table WCDK_PROCESS_FORM_BINDING
-- ----------------------------
CREATE INDEX `IDX_WCDK_PROCESS_FORM_BINDING_DEF_ID`
  ON `WCDK_PROCESS_FORM_BINDING` (`PROCESS_DEFINITION_ID`);
CREATE INDEX `IDX_WCDK_PROCESS_FORM_BINDING_DEF_KEY`
  ON `WCDK_PROCESS_FORM_BINDING` (`PROCESS_DEFINITION_KEY`);
CREATE INDEX `IDX_WCDK_PROCESS_FORM_BINDING_DEPLOY`
  ON `WCDK_PROCESS_FORM_BINDING` (`DEPLOYMENT_ID`);
CREATE INDEX `IDX_WCDK_PROCESS_FORM_BINDING_FORM`
  ON `WCDK_PROCESS_FORM_BINDING` (`FORM_ID`);
CREATE UNIQUE INDEX `UK_WCDK_PROCESS_FORM_BINDING_DEF`
  ON `WCDK_PROCESS_FORM_BINDING` (`PROCESS_DEFINITION_ID`, `BIND_SCOPE`, `TASK_DEFINITION_KEY`, `TENANT_ID`);

-- ----------------------------
-- Primary Key structure for table WCDK_PROCESS_REQUEST
-- ----------------------------
ALTER TABLE `WCDK_PROCESS_REQUEST` ADD PRIMARY KEY (`ID`);

-- ----------------------------
-- Indexes structure for table WCDK_PROCESS_REQUEST
-- ----------------------------
CREATE INDEX `IDX_WCDK_PROCESS_REQUEST_DEF_KEY`
  ON `WCDK_PROCESS_REQUEST` (`PROCESS_DEFINITION_KEY`);
CREATE INDEX `IDX_WCDK_PROCESS_REQUEST_PROC`
  ON `WCDK_PROCESS_REQUEST` (`PROCESS_INSTANCE_ID`);
CREATE INDEX `IDX_WCDK_PROCESS_REQUEST_STATUS`
  ON `WCDK_PROCESS_REQUEST` (`STATUS`);
CREATE UNIQUE INDEX `UK_WCDK_PROCESS_REQUEST_NO`
  ON `WCDK_PROCESS_REQUEST` (`PROCESS_NO`);

-- ----------------------------
-- Foreign Keys structure for table ACT_GE_BYTEARRAY
-- ----------------------------
ALTER TABLE `ACT_GE_BYTEARRAY` ADD FOREIGN KEY (`DEPLOYMENT_ID_`) REFERENCES `ACT_RE_DEPLOYMENT` (`ID_`) ON DELETE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table ACT_ID_MEMBERSHIP
-- ----------------------------
ALTER TABLE `ACT_ID_MEMBERSHIP` ADD FOREIGN KEY (`GROUP_ID_`) REFERENCES `ACT_ID_GROUP` (`ID_`) ON DELETE NO ACTION;
ALTER TABLE `ACT_ID_MEMBERSHIP` ADD FOREIGN KEY (`USER_ID_`) REFERENCES `ACT_ID_USER` (`ID_`) ON DELETE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table ACT_ID_PRIV_MAPPING
-- ----------------------------
ALTER TABLE `ACT_ID_PRIV_MAPPING` ADD FOREIGN KEY (`PRIV_ID_`) REFERENCES `ACT_ID_PRIV` (`ID_`) ON DELETE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table ACT_PROCDEF_INFO
-- ----------------------------
ALTER TABLE `ACT_PROCDEF_INFO` ADD FOREIGN KEY (`INFO_JSON_ID_`) REFERENCES `ACT_GE_BYTEARRAY` (`ID_`) ON DELETE NO ACTION;
ALTER TABLE `ACT_PROCDEF_INFO` ADD FOREIGN KEY (`PROC_DEF_ID_`) REFERENCES `ACT_RE_PROCDEF` (`ID_`) ON DELETE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table ACT_RE_MODEL
-- ----------------------------
ALTER TABLE `ACT_RE_MODEL` ADD FOREIGN KEY (`DEPLOYMENT_ID_`) REFERENCES `ACT_RE_DEPLOYMENT` (`ID_`) ON DELETE NO ACTION;
ALTER TABLE `ACT_RE_MODEL` ADD FOREIGN KEY (`EDITOR_SOURCE_VALUE_ID_`) REFERENCES `ACT_GE_BYTEARRAY` (`ID_`) ON DELETE NO ACTION;
ALTER TABLE `ACT_RE_MODEL` ADD FOREIGN KEY (`EDITOR_SOURCE_EXTRA_VALUE_ID_`) REFERENCES `ACT_GE_BYTEARRAY` (`ID_`) ON DELETE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table ACT_RU_DEADLETTER_JOB
-- ----------------------------
ALTER TABLE `ACT_RU_DEADLETTER_JOB` ADD FOREIGN KEY (`CUSTOM_VALUES_ID_`) REFERENCES `ACT_GE_BYTEARRAY` (`ID_`) ON DELETE NO ACTION;
ALTER TABLE `ACT_RU_DEADLETTER_JOB` ADD FOREIGN KEY (`EXCEPTION_STACK_ID_`) REFERENCES `ACT_GE_BYTEARRAY` (`ID_`) ON DELETE NO ACTION;
ALTER TABLE `ACT_RU_DEADLETTER_JOB` ADD FOREIGN KEY (`EXECUTION_ID_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`) ON DELETE NO ACTION;
ALTER TABLE `ACT_RU_DEADLETTER_JOB` ADD FOREIGN KEY (`PROC_DEF_ID_`) REFERENCES `ACT_RE_PROCDEF` (`ID_`) ON DELETE NO ACTION;
ALTER TABLE `ACT_RU_DEADLETTER_JOB` ADD FOREIGN KEY (`PROCESS_INSTANCE_ID_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`) ON DELETE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table ACT_RU_EVENT_SUBSCR
-- ----------------------------
ALTER TABLE `ACT_RU_EVENT_SUBSCR` ADD FOREIGN KEY (`EXECUTION_ID_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`) ON DELETE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table ACT_RU_EXECUTION
-- ----------------------------
ALTER TABLE `ACT_RU_EXECUTION` ADD FOREIGN KEY (`PARENT_ID_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`) ON DELETE NO ACTION;
ALTER TABLE `ACT_RU_EXECUTION` ADD FOREIGN KEY (`PROC_DEF_ID_`) REFERENCES `ACT_RE_PROCDEF` (`ID_`) ON DELETE NO ACTION;
ALTER TABLE `ACT_RU_EXECUTION` ADD FOREIGN KEY (`PROC_INST_ID_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`) ON DELETE NO ACTION;
ALTER TABLE `ACT_RU_EXECUTION` ADD FOREIGN KEY (`SUPER_EXEC_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`) ON DELETE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table ACT_RU_EXTERNAL_JOB
-- ----------------------------
ALTER TABLE `ACT_RU_EXTERNAL_JOB` ADD FOREIGN KEY (`CUSTOM_VALUES_ID_`) REFERENCES `ACT_GE_BYTEARRAY` (`ID_`) ON DELETE NO ACTION;
ALTER TABLE `ACT_RU_EXTERNAL_JOB` ADD FOREIGN KEY (`EXCEPTION_STACK_ID_`) REFERENCES `ACT_GE_BYTEARRAY` (`ID_`) ON DELETE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table ACT_RU_IDENTITYLINK
-- ----------------------------
ALTER TABLE `ACT_RU_IDENTITYLINK` ADD FOREIGN KEY (`PROC_DEF_ID_`) REFERENCES `ACT_RE_PROCDEF` (`ID_`) ON DELETE NO ACTION;
ALTER TABLE `ACT_RU_IDENTITYLINK` ADD FOREIGN KEY (`PROC_INST_ID_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`) ON DELETE NO ACTION;
ALTER TABLE `ACT_RU_IDENTITYLINK` ADD FOREIGN KEY (`TASK_ID_`) REFERENCES `ACT_RU_TASK` (`ID_`) ON DELETE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table ACT_RU_JOB
-- ----------------------------
ALTER TABLE `ACT_RU_JOB` ADD FOREIGN KEY (`CUSTOM_VALUES_ID_`) REFERENCES `ACT_GE_BYTEARRAY` (`ID_`) ON DELETE NO ACTION;
ALTER TABLE `ACT_RU_JOB` ADD FOREIGN KEY (`EXCEPTION_STACK_ID_`) REFERENCES `ACT_GE_BYTEARRAY` (`ID_`) ON DELETE NO ACTION;
ALTER TABLE `ACT_RU_JOB` ADD FOREIGN KEY (`EXECUTION_ID_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`) ON DELETE NO ACTION;
ALTER TABLE `ACT_RU_JOB` ADD FOREIGN KEY (`PROC_DEF_ID_`) REFERENCES `ACT_RE_PROCDEF` (`ID_`) ON DELETE NO ACTION;
ALTER TABLE `ACT_RU_JOB` ADD FOREIGN KEY (`PROCESS_INSTANCE_ID_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`) ON DELETE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table ACT_RU_SUSPENDED_JOB
-- ----------------------------
ALTER TABLE `ACT_RU_SUSPENDED_JOB` ADD FOREIGN KEY (`CUSTOM_VALUES_ID_`) REFERENCES `ACT_GE_BYTEARRAY` (`ID_`) ON DELETE NO ACTION;
ALTER TABLE `ACT_RU_SUSPENDED_JOB` ADD FOREIGN KEY (`EXCEPTION_STACK_ID_`) REFERENCES `ACT_GE_BYTEARRAY` (`ID_`) ON DELETE NO ACTION;
ALTER TABLE `ACT_RU_SUSPENDED_JOB` ADD FOREIGN KEY (`EXECUTION_ID_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`) ON DELETE NO ACTION;
ALTER TABLE `ACT_RU_SUSPENDED_JOB` ADD FOREIGN KEY (`PROC_DEF_ID_`) REFERENCES `ACT_RE_PROCDEF` (`ID_`) ON DELETE NO ACTION;
ALTER TABLE `ACT_RU_SUSPENDED_JOB` ADD FOREIGN KEY (`PROCESS_INSTANCE_ID_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`) ON DELETE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table ACT_RU_TASK
-- ----------------------------
ALTER TABLE `ACT_RU_TASK` ADD FOREIGN KEY (`EXECUTION_ID_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`) ON DELETE NO ACTION;
ALTER TABLE `ACT_RU_TASK` ADD FOREIGN KEY (`PROC_DEF_ID_`) REFERENCES `ACT_RE_PROCDEF` (`ID_`) ON DELETE NO ACTION;
ALTER TABLE `ACT_RU_TASK` ADD FOREIGN KEY (`PROC_INST_ID_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`) ON DELETE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table ACT_RU_TIMER_JOB
-- ----------------------------
ALTER TABLE `ACT_RU_TIMER_JOB` ADD FOREIGN KEY (`CUSTOM_VALUES_ID_`) REFERENCES `ACT_GE_BYTEARRAY` (`ID_`) ON DELETE NO ACTION;
ALTER TABLE `ACT_RU_TIMER_JOB` ADD FOREIGN KEY (`EXCEPTION_STACK_ID_`) REFERENCES `ACT_GE_BYTEARRAY` (`ID_`) ON DELETE NO ACTION;
ALTER TABLE `ACT_RU_TIMER_JOB` ADD FOREIGN KEY (`EXECUTION_ID_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`) ON DELETE NO ACTION;
ALTER TABLE `ACT_RU_TIMER_JOB` ADD FOREIGN KEY (`PROC_DEF_ID_`) REFERENCES `ACT_RE_PROCDEF` (`ID_`) ON DELETE NO ACTION;
ALTER TABLE `ACT_RU_TIMER_JOB` ADD FOREIGN KEY (`PROCESS_INSTANCE_ID_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`) ON DELETE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table ACT_RU_VARIABLE
-- ----------------------------
ALTER TABLE `ACT_RU_VARIABLE` ADD FOREIGN KEY (`BYTEARRAY_ID_`) REFERENCES `ACT_GE_BYTEARRAY` (`ID_`) ON DELETE NO ACTION;
ALTER TABLE `ACT_RU_VARIABLE` ADD FOREIGN KEY (`EXECUTION_ID_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`) ON DELETE NO ACTION;
ALTER TABLE `ACT_RU_VARIABLE` ADD FOREIGN KEY (`PROC_INST_ID_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`) ON DELETE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table FLW_EVENT_RESOURCE
-- ----------------------------
ALTER TABLE `FLW_EVENT_RESOURCE` ADD FOREIGN KEY (`DEPLOYMENT_ID_`) REFERENCES `FLW_EVENT_DEPLOYMENT` (`ID_`) ON DELETE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table FLW_RU_BATCH_PART
-- ----------------------------
ALTER TABLE `FLW_RU_BATCH_PART` ADD FOREIGN KEY (`BATCH_ID_`) REFERENCES `FLW_RU_BATCH` (`ID_`) ON DELETE NO ACTION;

SET FOREIGN_KEY_CHECKS = 1;
