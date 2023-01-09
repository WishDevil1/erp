#!/bin/sh

set -e

# We assume that the stuff was started with 10_reset_db_to_seed_dump.sh
# We assume that in the folder misc/dev-support/docker/infrastructure/env-files/ there exists an env file named ${BRANCH_NAME}.env
if ! [ -z "$1" ]; then
    BRANCH_NAME=$1
else
    echo "!! The first parameter needs do correspond to an env-File !!"
    echo "!! E.g. to use the env-file env-files/release.env, run 20_convert_db_to_template.sh release !!" 
    exit
fi

set -u

# the winpty is needed to avoid an error when running the script in git bash on windows

winpty docker exec -it ${BRANCH_NAME}_db  psql -U postgres -c "alter database metasfresh rename to metasfresh_template_master_integration;"
winpty docker exec -it ${BRANCH_NAME}_db  psql -U postgres -c "alter database metasfresh_template_master_integration is_template true;"

echo "The local database has been converted to a template database."
echo "You can drop this template database by running the following commands:"
echo ""
echo "winpty docker exec -it ${BRANCH_NAME}_db  psql -U postgres -c \"UPDATE pg_database SET datistemplate='false' WHERE datname='metasfresh_template_master_integration';\""
echo "winpty docker exec -it ${BRANCH_NAME}_db  psql -U postgres -c \"drop database if exists metasfresh_template_master_integration;\""
echo ""
echo "You can now proceed with creating the actual database from this template"