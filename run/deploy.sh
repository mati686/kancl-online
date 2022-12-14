#!/usr/bin/bash

GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# exit script on first error
set -e

function onError()
{
  echo -e
  echo -e "${RED}Failed${NC}"
}
trap onError ERR

echo ""
echo "Deploying"
echo "-------------"
echo ""

docker stop $(docker ps -q) | echo ""

docker volume create --name=sql_data | echo ""

cd run
docker-compose -f docker-compose.yml -f docker-compose-prod.yml up --build --detach
cd ..

docker container prune -f

echo -e ""
echo -e "${GREEN}Success${NC}"
