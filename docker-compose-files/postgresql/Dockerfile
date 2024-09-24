# Use the official PostgreSQL image from Docker Hub
FROM postgres:14

# Set environment variables
ENV POSTGRES_DB=igibgo
ENV POSTGRES_PASSWORD=Hellochixiyu07!

COPY init-script.sh /tmp/init-script.sh

RUN chmod +x /tmp/init-script.sh && /tmp/init-script.sh

# Expose the PostgreSQL port
EXPOSE 5432


# Add any additional configuration if needed
# COPY postgresql.conf /etc/postgresql/postgresql.conf

# Set the default command to run when starting the container
CMD ["postgres"]