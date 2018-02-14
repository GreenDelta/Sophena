Below the new database fields of Sophena 2.0 are listed. We need some kind of
database update and schema version for Sophena 2.0.

#### Table `tbl_heat_nets`

* `lower_buffer_load_temperature`
* `buffer_loss`

```sql
alter table tbl_heat_nets add column lower_buffer_load_temperature double;
alter table tbl_heat_nets add column buffer_loss double;
update tbl_heat_nets set buffer_loss = 0.15;
```

#### Table `tbl_building_states`

* `antifreezing_temperature`

```sql
alter table tbl_building_states add column antifreezing_temperature double;
```

Set it initially to 5°C:

```sql
update tbl_building_states set antifreezing_temperature = 5;
```

#### New Table `tbl_time_intervals`

```sql
CREATE TABLE tbl_time_intervals (
    
    id CHAR(36),
    f_owner CHAR(36),
    
    start_time VARCHAR(255),
    end_time VARCHAR(255),
    description VARCHAR(255),
    
    PRIMARY KEY (id)
);
```

#### Table `tbl_heat_net_pipes`

* `name`

```sql
alter table tbl_heat_net_pipes add column name VARCHAR(255);
```


#### New Table `tbl_producer_profiles`


```sql
CREATE TABLE tbl_producer_profiles (

    id CHAR(36),
    
    min_power BLOB (80 K),
    max_power BLOB (80 K),

    PRIMARY KEY (id)
);
```

#### Table `tbl_producers`

```sql
alter table tbl_producers add column has_profile BOOLEAN;
alter table tbl_producers add column f_profile CHAR(36);
alter table tbl_producers add column profile_max_power DOUBLE;
```


#### Nice to have

* HeatNet -> replace single interruption fields with TimeInterval reference @OneToOne
* use JPA Converter API and not the one from EclipseLink
