jetty:
	lein run

pg: 
	docker run -it --rm \
                --name=pg -p 9999:5432 \
                -v $(PWD)/data:/data \
                -e POSTGRES_USER=mary \
                -e POSTGRES_PASSWORD=abc \
                -e POSTGRES_DB=mydb \
                -e PGDATA=/data \
                postgres:10

psql: 
	psql -h localhost -p 9999 -U mary -d mydb

