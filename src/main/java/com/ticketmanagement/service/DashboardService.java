package com.ticketmanagement.service;

import com.ticketmanagement.dto.response.DashboardResponse;

import java.time.LocalDate;

public interface DashboardService {

    DashboardResponse getDashboard(String username);

    DashboardResponse getDashboard(String username, LocalDate reportDate);
}
