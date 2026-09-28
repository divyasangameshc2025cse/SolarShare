package PROJECT.SOLARSHARE.controller;

import PROJECT.SOLARSHARE.model.ConsumptionLog;
import PROJECT.SOLARSHARE.model.GenerationLog;
import PROJECT.SOLARSHARE.model.Household;
import PROJECT.SOLARSHARE.model.Installation;
import PROJECT.SOLARSHARE.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/")
public class WebViewController {

    private final InstallationService installationService;
    private final HouseholdService householdService;
    private final GenerationLogService generationLogService;
    private final ConsumptionLogService consumptionLogService;
    private final SummaryService summaryService;

    public WebViewController(InstallationService installationService,
                             HouseholdService householdService,
                             GenerationLogService generationLogService,
                             ConsumptionLogService consumptionLogService,
                             SummaryService summaryService) {
        this.installationService = installationService;
        this.householdService = householdService;
        this.generationLogService = generationLogService;
        this.consumptionLogService = consumptionLogService;
        this.summaryService = summaryService;
    }

    private String getHeaderAndNav(String activeTab, String message, String error) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>");
        sb.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        sb.append("<title>SOLAR SHARE – Community Rooftop Solar Usage Tracker</title>");
        sb.append("<link rel='stylesheet' href='/style.css'></head><body>");
        sb.append("<header><h1>SOLAR SHARE</h1><p>Community Rooftop Solar Usage Tracker</p></header>");
        sb.append("<nav>");
        sb.append("<a href='/' class='").append("home".equals(activeTab) ? "active" : "").append("'>Home</a>");
        sb.append("<a href='/installations' class='").append("installation".equals(activeTab) ? "active" : "").append("'>Installation</a>");
        sb.append("<a href='/households' class='").append("households".equals(activeTab) ? "active" : "").append("'>Households</a>");
        sb.append("<a href='/generation' class='").append("generation".equals(activeTab) ? "active" : "").append("'>Generation</a>");
        sb.append("<a href='/consumption' class='").append("consumption".equals(activeTab) ? "active" : "").append("'>Consumption</a>");
        sb.append("<a href='/summary' class='").append("summary".equals(activeTab) ? "active" : "").append("'>Monthly Summary</a>");
        sb.append("</nav><div class='container'>");

        if (message != null && !message.isBlank()) {
            sb.append("<div class='alert alert-success'>").append(escapeHtml(message)).append("</div>");
        }
        if (error != null && !error.isBlank()) {
            sb.append("<div class='alert alert-error'>").append(escapeHtml(error)).append("</div>");
        }
        return sb.toString();
    }

    private String getFooter() {
        return "</div><footer><p>SOLAR SHARE &copy; 2026 – Community Rooftop Solar Usage Tracker</p></footer></body></html>";
    }

    // ==========================================
    // 1. HOME VIEW
    // ==========================================
    @GetMapping({"", "/", "/home"})
    @ResponseBody
    public String home() {
        List<Installation> installations = installationService.getAllInstallations();
        List<Household> households = householdService.getAllHouseholds();
        List<GenerationLog> genLogs = generationLogService.getAllGenerationLogs();
        List<ConsumptionLog> consLogs = consumptionLogService.getAllConsumptionLogs();

        StringBuilder html = new StringBuilder();
        html.append(getHeaderAndNav("home", null, null));
        html.append("<div class='card'>");
        html.append("<h2>Welcome to SOLAR SHARE</h2>");
        html.append("<p>A housing community has a shared rooftop solar installation. The system records daily solar generation, ");
        html.append("household consumption, allocates common solar generation to households using fixed allocation ratios, ");
        html.append("tracks exported units, and shows monthly usage summaries.</p>");


        html.append("<h3>Quick Overview</h3>");
        html.append("<div class='summary-stats'>");
        html.append("<div class='stat-box'><div class='stat-title'>Total Installations</div><div class='stat-value'>").append(installations.size()).append("</div></div>");
        html.append("<div class='stat-box'><div class='stat-title'>Total Households</div><div class='stat-value'>").append(households.size()).append("</div></div>");
        html.append("<div class='stat-box'><div class='stat-title'>Generation Logs</div><div class='stat-value'>").append(genLogs.size()).append("</div></div>");
        html.append("<div class='stat-box'><div class='stat-title'>Consumption Logs</div><div class='stat-value'>").append(consLogs.size()).append("</div></div>");
        html.append("</div></div>");

        html.append(getFooter());
        return html.toString();
    }

    // ==========================================
    // 2. INSTALLATIONS VIEW & CRUD
    // ==========================================
    @GetMapping("/installations")
    @ResponseBody
    public String installations(@RequestParam(required = false) Long editId,
                                @RequestParam(required = false) String msg,
                                @RequestParam(required = false) String error) {
        List<Installation> list = installationService.getAllInstallations();
        Installation editItem = null;
        if (editId != null) {
            try {
                editItem = installationService.getInstallationById(editId);
            } catch (Exception ignored) {}
        }

        StringBuilder html = new StringBuilder();
        html.append(getHeaderAndNav("installation", msg, error));
        html.append("<div class='card'>");
        html.append("<h2>").append(editItem != null ? "Edit Installation" : "Manage Rooftop Solar Installation").append("</h2>");

        html.append("<form method='POST' action='/installations/save'>");
        if (editItem != null) {
            html.append("<input type='hidden' name='id' value='").append(editItem.getId()).append("'>");
        }
        html.append("<div class='form-grid'>");
        html.append("<div class='form-group'><label for='name'>Installation Name *</label>");
        html.append("<input type='text' id='name' name='name' value='").append(editItem != null ? escapeHtml(editItem.getName()) : "").append("' placeholder='e.g. Green Valley Solar' required></div>");

        html.append("<div class='form-group'><label for='location'>Location *</label>");
        html.append("<input type='text' id='location' name='location' value='").append(editItem != null ? escapeHtml(editItem.getLocation()) : "").append("' placeholder='e.g. Block A Rooftop' required></div>");

        html.append("<div class='form-group'><label for='capacityKw'>Capacity in kW *</label>");
        html.append("<input type='number' step='0.1' id='capacityKw' name='capacityKw' value='").append(editItem != null ? editItem.getCapacityKw() : "").append("' placeholder='e.g. 50.0' required min='0.1'></div>");
        html.append("</div>");

        html.append("<div class='btn-group'>");
        html.append("<button type='submit' class='btn btn-green'>").append(editItem != null ? "Update Installation" : "Add Installation").append("</button>");
        if (editItem != null) {
            html.append("<a href='/installations' class='btn btn-secondary'>Cancel</a>");
        }
        html.append("</div></form>");

        html.append("<h3>Existing Installations</h3>");
        html.append("<div class='table-responsive'><table><thead><tr><th>ID</th><th>Name</th><th>Location</th><th>Capacity (kW)</th><th>Actions</th></tr></thead><tbody>");
        if (list.isEmpty()) {
            html.append("<tr><td colspan='5'>No installations recorded yet.</td></tr>");
        } else {
            for (Installation inst : list) {
                html.append("<tr><td>").append(inst.getId()).append("</td>");
                html.append("<td><strong>").append(escapeHtml(inst.getName())).append("</strong></td>");
                html.append("<td>").append(escapeHtml(inst.getLocation())).append("</td>");
                html.append("<td>").append(inst.getCapacityKw()).append(" kW</td>");
                html.append("<td><a href='/installations?editId=").append(inst.getId()).append("' class='btn btn-green btn-sm'>Edit</a> ");
                html.append("<a href='/installations/delete/").append(inst.getId()).append("' class='btn btn-danger btn-sm' onclick='return confirm(\"Are you sure?\")'>Delete</a></td></tr>");
            }
        }
        html.append("</tbody></table></div></div>");

        html.append(getFooter());
        return html.toString();
    }

    @PostMapping("/installations/save")
    public String saveInstallation(@RequestParam(required = false) Long id,
                                   @RequestParam String name,
                                   @RequestParam String location,
                                   @RequestParam Double capacityKw) {
        try {
            Installation inst = new Installation(id, name, location, capacityKw);
            if (id != null) {
                installationService.updateInstallation(id, inst);
            } else {
                installationService.createInstallation(inst);
            }
            return "redirect:/installations?msg=Installation+saved+successfully";
        } catch (Exception ex) {
            return "redirect:/installations?error=" + encodeParam(ex.getMessage());
        }
    }

    @GetMapping("/installations/delete/{id}")
    public String deleteInstallation(@PathVariable Long id) {
        try {
            installationService.deleteInstallation(id);
            return "redirect:/installations?msg=Installation+deleted+successfully";
        } catch (Exception ex) {
            return "redirect:/installations?error=" + encodeParam(ex.getMessage());
        }
    }

    // ==========================================
    // 3. HOUSEHOLDS VIEW & CRUD
    // ==========================================
    @GetMapping("/households")
    @ResponseBody
    public String households(@RequestParam(required = false) Long editId,
                             @RequestParam(required = false) String msg,
                             @RequestParam(required = false) String error) {
        List<Household> list = householdService.getAllHouseholds();
        List<Installation> installations = installationService.getAllInstallations();
        Household editItem = null;
        if (editId != null) {
            try {
                editItem = householdService.getHouseholdById(editId);
            } catch (Exception ignored) {}
        }

        StringBuilder html = new StringBuilder();
        html.append(getHeaderAndNav("households", msg, error));
        html.append("<div class='card'>");
        html.append("<h2>").append(editItem != null ? "Edit Household" : "Manage Households").append("</h2>");

        html.append("<form method='POST' action='/households/save'>");
        if (editItem != null) {
            html.append("<input type='hidden' name='id' value='").append(editItem.getId()).append("'>");
        }
        html.append("<div class='form-grid'>");
        html.append("<div class='form-group'><label for='householdName'>Household Name *</label>");
        html.append("<input type='text' id='householdName' name='householdName' value='").append(editItem != null ? escapeHtml(editItem.getHouseholdName()) : "").append("' placeholder='e.g. Flat 101 - Sharma' required></div>");

        html.append("<div class='form-group'><label for='installationId'>Installation *</label>");
        html.append("<select id='installationId' name='installationId' required><option value=''>Select Installation</option>");
        for (Installation inst : installations) {
            boolean sel = editItem != null && editItem.getInstallation() != null && inst.getId().equals(editItem.getInstallation().getId());
            html.append("<option value='").append(inst.getId()).append("'").append(sel ? " selected" : "").append(">")
                .append(escapeHtml(inst.getName())).append(" (").append(inst.getCapacityKw()).append(" kW)</option>");
        }
        html.append("</select></div>");

        html.append("<div class='form-group'><label for='allocationRatio'>Allocation Ratio (e.g. 0.40 for 40%) *</label>");
        html.append("<input type='number' step='0.01' id='allocationRatio' name='allocationRatio' value='").append(editItem != null ? editItem.getAllocationRatio() : "").append("' placeholder='e.g. 0.30' required min='0.01' max='1.0'></div>");
        html.append("</div>");

        html.append("<div class='btn-group'>");
        html.append("<button type='submit' class='btn btn-green'>").append(editItem != null ? "Update Household" : "Add Household").append("</button>");
        if (editItem != null) {
            html.append("<a href='/households' class='btn btn-secondary'>Cancel</a>");
        }
        html.append("</div></form>");

        html.append("<h3>Registered Households</h3>");
        html.append("<div class='table-responsive'><table><thead><tr><th>ID</th><th>Household Name</th><th>Installation</th><th>Allocation Ratio</th><th>Actions</th></tr></thead><tbody>");
        if (list.isEmpty()) {
            html.append("<tr><td colspan='5'>No households recorded yet.</td></tr>");
        } else {
            for (Household h : list) {
                html.append("<tr><td>").append(h.getId()).append("</td>");
                html.append("<td><strong>").append(escapeHtml(h.getHouseholdName())).append("</strong></td>");
                html.append("<td>").append(h.getInstallation() != null ? escapeHtml(h.getInstallation().getName()) : "N/A").append("</td>");
                html.append("<td>").append(h.getAllocationRatio()).append(" (").append(Math.round(h.getAllocationRatio() * 100)).append("%)</td>");
                html.append("<td><a href='/households?editId=").append(h.getId()).append("' class='btn btn-green btn-sm'>Edit</a> ");
                html.append("<a href='/households/delete/").append(h.getId()).append("' class='btn btn-danger btn-sm' onclick='return confirm(\"Are you sure?\")'>Delete</a></td></tr>");
            }
        }
        html.append("</tbody></table></div></div>");

        html.append(getFooter());
        return html.toString();
    }

    @PostMapping("/households/save")
    public String saveHousehold(@RequestParam(required = false) Long id,
                                @RequestParam String householdName,
                                @RequestParam Long installationId,
                                @RequestParam Double allocationRatio) {
        try {
            Installation inst = new Installation();
            inst.setId(installationId);
            Household h = new Household(id, householdName, allocationRatio, inst);
            if (id != null) {
                householdService.updateHousehold(id, h);
            } else {
                householdService.createHousehold(h);
            }
            return "redirect:/households?msg=Household+saved+successfully";
        } catch (Exception ex) {
            return "redirect:/households?error=" + encodeParam(ex.getMessage());
        }
    }

    @GetMapping("/households/delete/{id}")
    public String deleteHousehold(@PathVariable Long id) {
        try {
            householdService.deleteHousehold(id);
            return "redirect:/households?msg=Household+deleted+successfully";
        } catch (Exception ex) {
            return "redirect:/households?error=" + encodeParam(ex.getMessage());
        }
    }

    // ==========================================
    // 4. GENERATION VIEW & CRUD
    // ==========================================
    @GetMapping("/generation")
    @ResponseBody
    public String generation(@RequestParam(required = false) Long editId,
                             @RequestParam(required = false) String msg,
                             @RequestParam(required = false) String error) {
        List<GenerationLog> list = generationLogService.getAllGenerationLogs();
        List<Installation> installations = installationService.getAllInstallations();
        GenerationLog editItem = null;
        if (editId != null) {
            try {
                editItem = generationLogService.getGenerationLogById(editId);
            } catch (Exception ignored) {}
        }

        StringBuilder html = new StringBuilder();
        html.append(getHeaderAndNav("generation", msg, error));
        html.append("<div class='card'>");
        html.append("<h2>").append(editItem != null ? "Edit Generation Log" : "Log Daily Solar Generation").append("</h2>");

        html.append("<form method='POST' action='/generation/save'>");
        if (editItem != null) {
            html.append("<input type='hidden' name='id' value='").append(editItem.getId()).append("'>");
        }
        html.append("<div class='form-grid'>");

        html.append("<div class='form-group'><label for='installationId'>Installation *</label>");
        html.append("<select id='installationId' name='installationId' required><option value=''>Select Installation</option>");
        for (Installation inst : installations) {
            boolean sel = editItem != null && editItem.getInstallation() != null && inst.getId().equals(editItem.getInstallation().getId());
            html.append("<option value='").append(inst.getId()).append("'").append(sel ? " selected" : "").append(">")
                .append(escapeHtml(inst.getName())).append(" (").append(inst.getCapacityKw()).append(" kW)</option>");
        }
        html.append("</select></div>");

        String defaultDate = editItem != null && editItem.getDate() != null ? editItem.getDate().toString() : LocalDate.now().toString();
        html.append("<div class='form-group'><label for='date'>Date *</label>");
        html.append("<input type='date' id='date' name='date' value='").append(defaultDate).append("' required></div>");

        html.append("<div class='form-group'><label for='generatedUnits'>Generated Units (kWh) *</label>");
        html.append("<input type='number' step='0.1' id='generatedUnits' name='generatedUnits' value='").append(editItem != null ? editItem.getGeneratedUnits() : "").append("' placeholder='e.g. 100.0' required min='0'></div>");
        html.append("</div>");

        html.append("<div class='btn-group'>");
        html.append("<button type='submit' class='btn btn-green'>").append(editItem != null ? "Update Generation Log" : "Add Generation Log").append("</button>");
        if (editItem != null) {
            html.append("<a href='/generation' class='btn btn-secondary'>Cancel</a>");
        }
        html.append("</div></form>");

        html.append("<h3>Daily Generation Logs</h3>");
        html.append("<div class='table-responsive'><table><thead><tr><th>ID</th><th>Installation</th><th>Date</th><th>Generated Units</th><th>Actions</th></tr></thead><tbody>");
        if (list.isEmpty()) {
            html.append("<tr><td colspan='5'>No generation logs recorded yet.</td></tr>");
        } else {
            for (GenerationLog g : list) {
                html.append("<tr><td>").append(g.getId()).append("</td>");
                html.append("<td>").append(g.getInstallation() != null ? escapeHtml(g.getInstallation().getName()) : "N/A").append("</td>");
                html.append("<td>").append(g.getDate()).append("</td>");
                html.append("<td><strong>").append(g.getGeneratedUnits()).append(" kWh</strong></td>");
                html.append("<td><a href='/generation?editId=").append(g.getId()).append("' class='btn btn-green btn-sm'>Edit</a> ");
                html.append("<a href='/generation/delete/").append(g.getId()).append("' class='btn btn-danger btn-sm' onclick='return confirm(\"Are you sure?\")'>Delete</a></td></tr>");
            }
        }
        html.append("</tbody></table></div></div>");

        html.append(getFooter());
        return html.toString();
    }

    @PostMapping("/generation/save")
    public String saveGeneration(@RequestParam(required = false) Long id,
                                 @RequestParam Long installationId,
                                 @RequestParam String date,
                                 @RequestParam Double generatedUnits) {
        try {
            Installation inst = new Installation();
            inst.setId(installationId);
            GenerationLog g = new GenerationLog(id, LocalDate.parse(date), generatedUnits, inst);
            if (id != null) {
                generationLogService.updateGenerationLog(id, g);
            } else {
                generationLogService.createGenerationLog(g);
            }
            return "redirect:/generation?msg=Generation+log+saved+successfully";
        } catch (Exception ex) {
            return "redirect:/generation?error=" + encodeParam(ex.getMessage());
        }
    }

    @GetMapping("/generation/delete/{id}")
    public String deleteGeneration(@PathVariable Long id) {
        try {
            generationLogService.deleteGenerationLog(id);
            return "redirect:/generation?msg=Generation+log+deleted+successfully";
        } catch (Exception ex) {
            return "redirect:/generation?error=" + encodeParam(ex.getMessage());
        }
    }

    // ==========================================
    // 5. CONSUMPTION VIEW & CRUD
    // ==========================================
    @GetMapping("/consumption")
    @ResponseBody
    public String consumption(@RequestParam(required = false) Long editId,
                              @RequestParam(required = false) String msg,
                              @RequestParam(required = false) String error) {
        List<ConsumptionLog> list = consumptionLogService.getAllConsumptionLogs();
        List<Household> households = householdService.getAllHouseholds();
        List<GenerationLog> genLogs = generationLogService.getAllGenerationLogs();
        ConsumptionLog editItem = null;
        if (editId != null) {
            try {
                editItem = consumptionLogService.getConsumptionLogById(editId);
            } catch (Exception ignored) {}
        }

        StringBuilder html = new StringBuilder();
        html.append(getHeaderAndNav("consumption", msg, error));
        html.append("<div class='card'>");
        html.append("<h2>").append(editItem != null ? "Edit Consumption Log" : "Log Household Electricity Consumption").append("</h2>");


        html.append("<form method='POST' action='/consumption/save'>");
        if (editItem != null) {
            html.append("<input type='hidden' name='id' value='").append(editItem.getId()).append("'>");
        }
        html.append("<div class='form-grid'>");

        html.append("<div class='form-group'><label for='householdId'>Household *</label>");
        html.append("<select id='householdId' name='householdId' required><option value=''>Select Household</option>");
        for (Household h : households) {
            boolean sel = editItem != null && editItem.getHousehold() != null && h.getId().equals(editItem.getHousehold().getId());
            html.append("<option value='").append(h.getId()).append("'").append(sel ? " selected" : "").append(">")
                .append(escapeHtml(h.getHouseholdName())).append(" (Ratio: ").append(h.getAllocationRatio()).append(")</option>");
        }
        html.append("</select></div>");

        html.append("<div class='form-group'><label for='generationLogId'>Generation Day *</label>");
        html.append("<select id='generationLogId' name='generationLogId' required><option value=''>Select Generation Log</option>");
        for (GenerationLog g : genLogs) {
            boolean sel = editItem != null && editItem.getGenerationLog() != null && g.getId().equals(editItem.getGenerationLog().getId());
            html.append("<option value='").append(g.getId()).append("'").append(sel ? " selected" : "").append(">")
                .append("Log #").append(g.getId()).append(": ").append(g.getDate()).append(" (").append(g.getGeneratedUnits()).append(" units - ")
                .append(g.getInstallation() != null ? escapeHtml(g.getInstallation().getName()) : "").append(")</option>");
        }
        html.append("</select></div>");

        String defaultDate = editItem != null && editItem.getDate() != null ? editItem.getDate().toString() : LocalDate.now().toString();
        html.append("<div class='form-group'><label for='date'>Consumption Date *</label>");
        html.append("<input type='date' id='date' name='date' value='").append(defaultDate).append("' required></div>");

        html.append("<div class='form-group'><label for='unitsConsumed'>Units Consumed (kWh) *</label>");
        html.append("<input type='number' step='0.1' id='unitsConsumed' name='unitsConsumed' value='").append(editItem != null ? editItem.getUnitsConsumed() : "").append("' placeholder='e.g. 25.0' required min='0'></div>");
        html.append("</div>");

        html.append("<div class='btn-group'>");
        html.append("<button type='submit' class='btn btn-green'>").append(editItem != null ? "Update Consumption Log" : "Save Consumption Log").append("</button>");
        if (editItem != null) {
            html.append("<a href='/consumption' class='btn btn-secondary'>Cancel</a>");
        }
        html.append("</div></form>");

        html.append("<h3>Consumption Logs</h3>");
        html.append("<div class='table-responsive'><table><thead><tr><th>ID</th><th>Household</th><th>Gen Log Day</th><th>Date</th><th>Units Consumed</th><th>Allocated Share</th><th>Actions</th></tr></thead><tbody>");
        if (list.isEmpty()) {
            html.append("<tr><td colspan='7'>No consumption logs recorded yet.</td></tr>");
        } else {
            for (ConsumptionLog c : list) {
                html.append("<tr><td>").append(c.getId()).append("</td>");
                html.append("<td><strong>").append(c.getHousehold() != null ? escapeHtml(c.getHousehold().getHouseholdName()) : "N/A").append("</strong></td>");
                html.append("<td>Log #").append(c.getGenerationLog() != null ? c.getGenerationLog().getId() : "N/A").append(" (").append(c.getGenerationLog() != null ? c.getGenerationLog().getDate() : "").append(")</td>");
                html.append("<td>").append(c.getDate()).append("</td>");
                html.append("<td>").append(c.getUnitsConsumed()).append(" units</td>");
                html.append("<td style='color:#2e7d32; font-weight:bold;'>").append(c.getAllocatedShare()).append(" units</td>");
                html.append("<td><a href='/consumption?editId=").append(c.getId()).append("' class='btn btn-green btn-sm'>Edit</a> ");
                html.append("<a href='/consumption/delete/").append(c.getId()).append("' class='btn btn-danger btn-sm' onclick='return confirm(\"Are you sure?\")'>Delete</a></td></tr>");
            }
        }
        html.append("</tbody></table></div></div>");

        html.append(getFooter());
        return html.toString();
    }

    @PostMapping("/consumption/save")
    public String saveConsumption(@RequestParam(required = false) Long id,
                                  @RequestParam Long householdId,
                                  @RequestParam Long generationLogId,
                                  @RequestParam String date,
                                  @RequestParam Double unitsConsumed) {
        try {
            Household h = new Household();
            h.setId(householdId);
            GenerationLog g = new GenerationLog();
            g.setId(generationLogId);

            ConsumptionLog c = new ConsumptionLog();
            c.setId(id);
            c.setDate(LocalDate.parse(date));
            c.setUnitsConsumed(unitsConsumed);
            c.setHousehold(h);
            c.setGenerationLog(g);

            ConsumptionLog saved;
            if (id != null) {
                saved = consumptionLogService.updateConsumptionLog(id, c);
            } else {
                saved = consumptionLogService.createConsumptionLog(c);
            }
            return "redirect:/consumption?msg=" + encodeParam("Consumption recorded! Allocated: " + saved.getAllocatedShare() + " units | Exported: " + saved.getExportedUnits() + " units.");
        } catch (Exception ex) {
            return "redirect:/consumption?error=" + encodeParam(ex.getMessage());
        }
    }

    @GetMapping("/consumption/delete/{id}")
    public String deleteConsumption(@PathVariable Long id) {
        try {
            consumptionLogService.deleteConsumptionLog(id);
            return "redirect:/consumption?msg=Consumption+log+deleted+successfully";
        } catch (Exception ex) {
            return "redirect:/consumption?error=" + encodeParam(ex.getMessage());
        }
    }

    // ==========================================
    // 6. MONTHLY SUMMARY VIEW (JDBCTEMPLATE)
    // ==========================================
    @GetMapping("/summary")
    @ResponseBody
    public String summary(@RequestParam(required = false) Long householdId,
                          @RequestParam(required = false, defaultValue = "9") Integer month,
                          @RequestParam(required = false, defaultValue = "2026") Integer year,
                          @RequestParam(required = false) String msg,
                          @RequestParam(required = false) String error) {
        List<Household> households = householdService.getAllHouseholds();
        Map<String, Object> summaryData = null;

        if (householdId != null) {
            try {
                summaryData = summaryService.getMonthlySummary(householdId, month, year);
            } catch (Exception ex) {
                error = ex.getMessage();
            }
        }

        StringBuilder html = new StringBuilder();
        html.append(getHeaderAndNav("summary", msg, error));
        html.append("<div class='card'>");
        html.append("<h2>Monthly Net Usage Summary</h2>");

        html.append("<form method='GET' action='/summary'>");
        html.append("<div class='form-grid'>");

        html.append("<div class='form-group'><label for='householdId'>Select Household *</label>");
        html.append("<select id='householdId' name='householdId' required><option value=''>Select Household</option>");
        for (Household h : households) {
            boolean sel = householdId != null && h.getId().equals(householdId);
            html.append("<option value='").append(h.getId()).append("'").append(sel ? " selected" : "").append(">")
                .append(escapeHtml(h.getHouseholdName())).append(" (Ratio: ").append(h.getAllocationRatio()).append(")</option>");
        }
        html.append("</select></div>");

        html.append("<div class='form-group'><label for='month'>Month *</label>");
        html.append("<select id='month' name='month' required>");
        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        for (int i = 1; i <= 12; i++) {
            boolean sel = (month != null && month == i) || (month == null && i == 9);
            html.append("<option value='").append(i).append("'").append(sel ? " selected" : "").append(">")
                .append(months[i - 1]).append(" (").append(i).append(")</option>");
        }
        html.append("</select></div>");

        html.append("<div class='form-group'><label for='year'>Year *</label>");
        html.append("<input type='number' id='year' name='year' value='").append(year != null ? year : 2026).append("' required min='2000' max='2100'></div>");
        html.append("</div>");

        html.append("<div class='btn-group'>");
        html.append("<button type='submit' class='btn btn-green'>Get Monthly Summary</button>");
        html.append("</div></form>");

        if (summaryData != null) {
            html.append("<div style='margin-top: 24px;'>");
            html.append("<h3>Summary Report for ").append(escapeHtml((String) summaryData.get("householdName"))).append(" (").append(month).append("/").append(year).append(")</h3>");

            html.append("<div class='table-responsive'><table><thead><tr><th>Household</th><th>Allocated Units</th><th>Consumed Units</th><th>Exported Units</th></tr></thead><tbody>");
            html.append("<tr><td>").append(escapeHtml((String) summaryData.get("householdName"))).append("</td>");
            html.append("<td style='color:#2e7d32; font-weight:bold;'>").append(summaryData.get("totalAllocatedUnits")).append(" units</td>");
            html.append("<td>").append(summaryData.get("totalConsumedUnits")).append(" units</td>");
            html.append("<td style='color:#1565c0; font-weight:bold;'>").append(summaryData.get("totalExportedUnits")).append(" units</td></tr>");
            html.append("</tbody></table></div>");

            html.append("<div class='summary-stats'>");
            html.append("<div class='stat-box'><div class='stat-title'>Total Allocated Share</div><div class='stat-value'>").append(summaryData.get("totalAllocatedUnits")).append("</div></div>");
            html.append("<div class='stat-box'><div class='stat-title'>Total Consumed Units</div><div class='stat-value'>").append(summaryData.get("totalConsumedUnits")).append("</div></div>");
            html.append("<div class='stat-box'><div class='stat-title'>Total Exported Units</div><div class='stat-value'>").append(summaryData.get("totalExportedUnits")).append("</div></div>");
            html.append("</div></div>");
        }

        html.append("</div>");
        html.append(getFooter());
        return html.toString();
    }

    // Helper: Escape HTML
    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }

    private String encodeParam(String str) {
        if (str == null) return "";
        try {
            return java.net.URLEncoder.encode(str, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return str.replace(" ", "+");
        }
    }
}
