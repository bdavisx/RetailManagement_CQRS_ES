CREATE DATABASE retail_management
  WITH OWNER = postgres
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       LC_COLLATE = 'English_United States.1252'
       LC_CTYPE = 'English_United States.1252'
       CONNECTION LIMIT = -1;

drop table postal_codes;

create table postal_codes (
    country_code char(2) not null,
    postal_code varchar(20) not null,
    locality varchar(180),
    region_name varchar(100),
    region_code varchar(20),
    county_province_name varchar(100),
    county_province_code varchar(20),
    community_name varchar(100),
    community_code varchar(20),
    latitude decimal( 7, 4 ),
    longitude decimal( 7, 4 ),
    accuracy smallint
);

create index postal_codes_country_code_postal_code on postal_codes (country_code, postal_code);
