<h1>Importing and Creating the Postal Code Database Table</h1>

<p>I downloaded the <a href="http://download.geonames.org/export/zip/allCountries.zip">allCountries.zip</a> file from <a
    href="http://www.geonames.org/">geonames.org</a>. </p>

<p>First we need to create a Postgresql database since we haven't dont that yet. I simply created a default local
    database with the name 'retail_management'. We will just need to make sure we keep our system flexible enough that
    we can point our repositories to whatever connection we need (including the possibility of different connections for
    different repositories. I'm going to put my DDL files in the <code>src/main/db</code> directory, in case you want to
    see them.</p>

<p>According to the <a href="http://download.geonames.org/export/zip/readme.txt">readme.txt</a> file from geonames, the
    format for the file is tab delimited with the following fields:</p>

<pre>
The data format is tab-delimited text in utf8 encoding, with the following fields :

country code      : iso country code, 2 characters
postal code       : varchar(20)
place name        : varchar(180)
admin name1       : 1. order subdivision (state) varchar(100)
admin code1       : 1. order subdivision (state) varchar(20)
admin name2       : 2. order subdivision (county/province) varchar(100)
admin code2       : 2. order subdivision (county/province) varchar(20)
admin name3       : 3. order subdivision (community) varchar(100)
admin code3       : 3. order subdivision (community) varchar(20)
latitude          : estimated latitude (wgs84)
longitude         : estimated longitude (wgs84)
accuracy          : accuracy of lat/lng from 1=estimated to 6=centroid
</pre>

<p>So we will create a table with the same format and import it. If we decide that we don't want parts of the table later
we can always optimize then. This is the initial cut at the table design:</p>

<pre class="prettify">
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
)

create index postal_codes_country_code_postal_code on postal_codes (country_code, postal_code)
</pre>

<p>Of course, there were some issues with the file and directly importing it to the table. Nothing major, but after I
    cleaned up the file and got it imported, I ran a backup against the table and put it in the <a
        href="../downloads/postal_codes_backup.zip">postal_codes_backup.zip</a> file. You might find it easier to use
    that backup file to restore the data.</p>
