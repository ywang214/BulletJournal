#!/bin/sh

echo -e '\n==>step 1 / 2: create data volume mount points'
mkdir -p "$HOME/docker/volumes/postgres"
mkdir -p "$HOME/docker/volumes/log"
mkdir -p "$HOME/docker/volumes/elasticsearch"

echo -e '\n==>step 2 / 2: docker-compose up -d'
docker-compose -f docker-compose.yml -f ../elk/docker-compose-prod.yml up -d

