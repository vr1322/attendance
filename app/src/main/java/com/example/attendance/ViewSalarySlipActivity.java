package com.example.attendance;

import android.app.ProgressDialog;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.OutputStream;

public class ViewSalarySlipActivity extends AppCompatActivity {

    TextView tvEmpName, tvDesignation, tvMonth, tvBasicPay, tvOvertime, tvDeduction,
            tvAdvance, tvUniform, tvFine, tvNetSalary, tvPresent, tvAbsent, tvTotal, tvPF, tvESIC_PT;
    ProgressDialog progressDialog;
    Button btnDownload, btnPrint;
    ScrollView scrollView;

    String empId, companyCode, month;

    // Salary data
    String employeeName, designation, monthYear, basicPay, overtimePay, deduction,
            advance, uniform, fine, netSalary, presentDays, absentDays, totalDays, pf, esicPT;

    // Company name
    String companyName = ""; // Will be fetched from API

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_salary_slip);

        tvEmpName = findViewById(R.id.tvEmpName);
        tvDesignation = findViewById(R.id.tvDesignation);
        tvMonth = findViewById(R.id.tvMonth);
        tvBasicPay = findViewById(R.id.tvBasicPay);
        tvOvertime = findViewById(R.id.tvOvertime);
        tvDeduction = findViewById(R.id.tvDeduction);
        tvAdvance = findViewById(R.id.tvAdvance);
        tvUniform = findViewById(R.id.tvUniform);
        tvFine = findViewById(R.id.tvFine);
        tvNetSalary = findViewById(R.id.tvNetSalary);
        tvPresent = findViewById(R.id.tvPresent);
        tvAbsent = findViewById(R.id.tvAbsent);
        tvTotal = findViewById(R.id.tvTotal);
        tvPF = findViewById(R.id.tvPF);
        tvESIC_PT = findViewById(R.id.tvESIC_PT);

        btnDownload = findViewById(R.id.btnDownload);
        btnPrint = findViewById(R.id.btnPrint);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading Slip...");
        progressDialog.setCancelable(false);

        empId = getIntent().getStringExtra("employee_id");
        companyCode = getIntent().getStringExtra("company_code");
        month = getIntent().getStringExtra("month");

        fetchSalarySlip();

        btnDownload.setOnClickListener(v -> generatePDF());
        btnPrint.setOnClickListener(v -> printSalarySlip());
    }

    private void fetchSalarySlip() {
        progressDialog.show();
        String url = "https://devonix.io/ems_api/view_salary_slip.php?employee_id=" + empId + "&month=" + month;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    progressDialog.dismiss();
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (!obj.optString("status").equals("success")) {
                            Toast.makeText(this, obj.optString("message", "No data"), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        JSONObject data = obj.getJSONObject("data");

                        // Store data for PDF/Print
                        companyName = data.optString("company_name", "Your Company Name");
                        employeeName = data.optString("employee_name");
                        designation = data.optString("designation");
                        monthYear = data.optString("month_year");
                        basicPay = data.optString("basic_pay");
                        overtimePay = data.optString("overtime_pay");
                        deduction = data.optString("deduction");
                        advance = data.optString("advance");
                        uniform = data.optString("uniform");
                        fine = data.optString("fine");
                        netSalary = data.optString("net_salary");
                        presentDays = data.optString("present_days");
                        absentDays = data.optString("absent_days");
                        totalDays = data.optString("total_days");
                        pf = data.optString("pf", "0");
                        esicPT = data.optString("esic_pt", "0");

                        // Bind UI
                        tvEmpName.setText("Name: " + employeeName);
                        tvDesignation.setText("Designation: " + designation);
                        tvMonth.setText("Month: " + monthYear);
                        tvBasicPay.setText("Basic Pay: ₹" + basicPay);
                        tvOvertime.setText("Overtime: ₹" + overtimePay);
                        tvDeduction.setText("Deduction: ₹" + deduction);
                        tvAdvance.setText("Advance: ₹" + advance);
                        tvUniform.setText("Uniform: ₹" + uniform);
                        tvPF.setText("PF: ₹" + pf);
                        tvESIC_PT.setText("ESIC/PT: ₹" + esicPT);
                        tvFine.setText("Fine: ₹" + fine);
                        tvNetSalary.setText("Net Salary: ₹" + netSalary);
                        tvPresent.setText(presentDays);
                        tvAbsent.setText(absentDays);
                        tvTotal.setText(totalDays);

                    } catch (Exception e) {
                        Toast.makeText(this, "Parsing Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Volley Error: " + error.toString(), Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void generatePDF() {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // A4
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        int startX = 40;
        int startY = 50;
        int lineHeight = 40;

        paint.setTextSize(22);
        paint.setFakeBoldText(true);
        float companyTextWidth = paint.measureText(companyName);
        canvas.drawText(companyName, (595 - companyTextWidth)/2, startY, paint);

        paint.setTextSize(18);
        paint.setFakeBoldText(true);
        String title = "Salary Slip";
        float titleWidth = paint.measureText(title);
        canvas.drawText(title, (595 - titleWidth)/2, startY + 50, paint);
        paint.setFakeBoldText(false);

        int y = startY + 100;
        paint.setTextSize(14);

        canvas.drawText("Employee Name: " + employeeName, startX, y, paint);
        y += lineHeight;
        canvas.drawText("Designation: " + designation, startX, y, paint);
        y += lineHeight;
        canvas.drawText("Month: " + monthYear, startX, y, paint);
        y += lineHeight;

        canvas.drawLine(startX, y, 555, y, paint);
        y += 10;
        canvas.drawText("Earnings / Deductions", startX + 10, y, paint);
        canvas.drawText("Amount (₹)", startX + 300, y, paint);
        y += lineHeight;
        canvas.drawLine(startX, y - 20, 555, y - 20, paint);

        String[][] rows = {
                {"Basic Pay", basicPay},
                {"Overtime", overtimePay},
                {"Advance", advance},
                {"Uniform", uniform},
                {"Deduction", deduction},
                {"PF", pf},
                {"ESIC/PT", esicPT},
                {"Fine", fine}
        };

        for (String[] row : rows) {
            canvas.drawText(row[0], startX + 10, y, paint);
            canvas.drawText(row[1], startX + 300, y, paint);
            y += lineHeight;
        }

        canvas.drawLine(startX, y, 555, y, paint);
        y += lineHeight;

        paint.setTextSize(16);
        canvas.drawText("Net Salary: ₹" + netSalary, startX + 150, y, paint);
        y += lineHeight;

        paint.setTextSize(14);
        canvas.drawText("Total Days: " + totalDays + ", Present: " + presentDays + ", Absent: " + absentDays, startX, y, paint);

        document.finishPage(page);

        try {
            android.content.ContentValues values = new android.content.ContentValues();
            values.put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, "SalarySlip_" + empId + ".pdf");
            values.put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
            values.put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            android.net.Uri uri = getContentResolver().insert(android.provider.MediaStore.Files.getContentUri("external"), values);
            OutputStream out = getContentResolver().openOutputStream(uri);
            document.writeTo(out);
            out.close();

            Toast.makeText(this, "PDF saved in Downloads", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        document.close();
    }

    private void printSalarySlip() {
        PrintManager printManager = (PrintManager) getSystemService(PRINT_SERVICE);

        PrintDocumentAdapter printAdapter = new PrintDocumentAdapter() {
            @Override
            public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes,
                                 android.os.CancellationSignal cancellationSignal,
                                 LayoutResultCallback callback, Bundle extras) {
                PrintDocumentInfo info = new PrintDocumentInfo.Builder("SalarySlip.pdf")
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .build();
                callback.onLayoutFinished(info, true);
            }

            @Override
            public void onWrite(android.print.PageRange[] pages,
                                android.os.ParcelFileDescriptor destination,
                                android.os.CancellationSignal cancellationSignal,
                                WriteResultCallback callback) {

                PdfDocument document = new PdfDocument();
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
                PdfDocument.Page page = document.startPage(pageInfo);
                Canvas canvas = page.getCanvas();
                Paint paint = new Paint();

                int startX = 40;
                int startY = 50;
                int lineHeight = 40;

                paint.setTextSize(22);
                paint.setFakeBoldText(true);
                float companyTextWidth = paint.measureText(companyName);
                canvas.drawText(companyName, (595 - companyTextWidth)/2, startY, paint);

                paint.setTextSize(18);
                paint.setFakeBoldText(true);
                String title = "Salary Slip";
                float titleWidth = paint.measureText(title);
                canvas.drawText(title, (595 - titleWidth)/2, startY + 50, paint);
                paint.setFakeBoldText(false);

                int y = startY + 100;
                paint.setTextSize(14);

                canvas.drawText("Employee Name: " + employeeName, startX, y, paint);
                y += lineHeight;
                canvas.drawText("Designation: " + designation, startX, y, paint);
                y += lineHeight;
                canvas.drawText("Month: " + monthYear, startX, y, paint);
                y += lineHeight;

                canvas.drawLine(startX, y, 555, y, paint);
                y += 10;
                canvas.drawText("Earnings / Deductions", startX + 10, y, paint);
                canvas.drawText("Amount (₹)", startX + 300, y, paint);
                y += lineHeight;
                canvas.drawLine(startX, y - 20, 555, y - 20, paint);

                String[][] rows = {
                        {"Basic Pay", basicPay},
                        {"Overtime", overtimePay},
                        {"Advance", advance},
                        {"Uniform", uniform},
                        {"Deduction", deduction},
                        {"PF", pf},
                        {"ESIC/PT", esicPT},
                        {"Fine", fine}
                };

                for (String[] row : rows) {
                    canvas.drawText(row[0], startX + 10, y, paint);
                    canvas.drawText(row[1], startX + 300, y, paint);
                    y += lineHeight;
                }

                canvas.drawLine(startX, y, 555, y, paint);
                y += lineHeight;

                paint.setTextSize(16);
                canvas.drawText("Net Salary: ₹" + netSalary, startX + 150, y, paint);
                y += lineHeight;

                paint.setTextSize(14);
                canvas.drawText("Total Days: " + totalDays + ", Present: " + presentDays + ", Absent: " + absentDays, startX, y, paint);

                document.finishPage(page);

                try {
                    OutputStream out = new java.io.FileOutputStream(destination.getFileDescriptor());
                    document.writeTo(out);
                    document.close();
                    callback.onWriteFinished(new android.print.PageRange[]{android.print.PageRange.ALL_PAGES});
                } catch (Exception e) {
                    callback.onWriteFailed(e.toString());
                }
            }
        };

        printManager.print("SalarySlip", printAdapter, null);
    }
}
