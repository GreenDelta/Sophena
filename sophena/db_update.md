Below the new database fields of Sophena 2.0 are listed. We need some kind of
database update and schema version for Sophena 2.0.

#### Table `tbl_heat_nets`

* `lower_buffer_load_temperature`
* `buffer_loss`

```
alter table tbl_heat_nets add column lower_buffer_load_temperature double;
alter table tbl_heat_nets add column buffer_loss double;
update tbl_heat_nets set buffer_loss = 0.15;
```
