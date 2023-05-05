# vamtyc

![example workflow](https://github.com/guillerglez88/vamtyc/actions/workflows/ci.yml/badge.svg)

REST on values 

## Features

- REST 
  - Methods
    - GET
    - POST
    - PUT
    - DELETE
  - Resources
    - Resource
    - Route
    - Queryp

- Custom
  - Url search via query-strings
    - match exact
    - match contains
  - Pagination via `_offset` & `_limit` queryps
  - Reduce payload via `_fields` queryp
  - Resources
    - Coding
    - List
    - Seq
    - PgQuery

## TODO

- etag: rest e-tag header
- profile: validation based on spec
- history: request history by entity
- browser: vamptyc client
- patch: partially update resource
- transaction: bulk requests
- result fields path remapping
- date search
- number search
- redefine PgQuery sql
