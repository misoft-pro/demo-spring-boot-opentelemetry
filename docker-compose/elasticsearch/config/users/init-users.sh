#!/bin/bash

# Wait until Elasticsearch is up and running
until curl -s -f -u elastic:changeme http://elasticsearch:9200/_cluster/health; do
  echo "Waiting for Elasticsearch to be up..."
  sleep 5
done

# Create kibana_system user
curl -X POST "http://elastic:changeme@elasticsearch:9200/_security/user/kibana_system" -H 'Content-Type: application/json' -d'
{
  "password" : "my_kibana_password",
  "roles" : [ "kibana_system" ],
  "full_name" : "Kibana System User"
}
'

echo "Users created successfully."
