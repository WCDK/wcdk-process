package com.wcdk.process.config;

import com.wcdk.r2dbc.BaseRepository;
import com.wcdk.r2dbc.Repository;

@Repository
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public interface SchemaVersionRepository extends BaseRepository<SchemaVersionEntity> {

}