package com.lounes.gestion_reservations.dto;

import jakarta.validation.constraints.NotNull;

public class FileAttenteRequest {
    @NotNull
    private Long clientId;
    @NotNull
    private Long employeId;
    @NotNull
    private Long serviceId;
    @NotNull
    private Long reservationId;

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public Long getEmployeId() { return employeId; }
    public void setEmployeId(Long employeId) { this.employeId = employeId; }
    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }
    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }
}