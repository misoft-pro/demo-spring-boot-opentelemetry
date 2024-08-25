FROM otel/opentelemetry-collector-contrib:latest

# Copy the configuration file into the container
COPY otel-collector-config.yaml /etc/otel/otel-collector-config.yaml

# Set environment variables
ENV LOG_LEVEL=DEBUG

# Expose the necessary ports
EXPOSE 4317
EXPOSE 4318
EXPOSE 55681

# Set the default command to run the OpenTelemetry Collector with the specified configuration
CMD ["--config=/etc/otel/otel-collector-config.yaml"]
