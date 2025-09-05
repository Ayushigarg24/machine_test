import java.sql.*;
import java.util.Scanner;

public class PlacementTest {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        String url = "jdbc:mysql://localhost:3306/placement_test";
        String user = "root";       
        String password = "8077510407";   

        try (Connection con = DriverManager.getConnection(url, user, password)) {
            System.out.println(" Connected to DB successfully!");

            // ---- Input + validation ----
           
            String studentName ;
            while(true){
                    System.out.print("Enter Student Name (max 30): ");
                    studentName = sc.nextLine().trim();
                    if(studentName.length() <= 30 && !studentName.isEmpty()){
                        break;
                    }
                    else{
                        System.out.println("Invalid name | Please try again");
                    }
            }

             String collegeName ;
            while(true){
                    System.out.print("Enter College Name (max 50): ");
                    collegeName = sc.nextLine().trim();
                    if(collegeName.length() <= 50 && !collegeName.isEmpty()){
                        break;
                    }
                    else{
                        System.out.println("Invalid name | Please try again");
                    }
            }
            // if (studentName.isEmpty() || studentName.length() > 30) {
            //     System.out.println(" Invalid Student Name."); return;
            // }

            // System.out.print("Enter College Name (max 50): ");
            // String collegeName = sc.nextLine().trim();
            // if (collegeName.isEmpty() || collegeName.length() > 50) {
            //     System.out.println(" Invalid College Name."); return;
            // }
             float r1 ;
            while(true){
                    System.out.print("Enter Round1 Marks (0-10): ");
                     r1 = sc.nextFloat();
                    if (r1 > 0 && r1 < 10){
                        break;
                    }
                    else{
                        System.out.println(" Invalid Round1.");
                    }
            }

            // System.out.print("Enter Round1 Marks (0-10): ");
            // float r1 = sc.nextFloat();
            // if (r1 < 0 || r1 > 10) { System.out.println(" Invalid Round1."); return; }

            float r2 ;
            while(true){
                    System.out.print("Enter Round2 Marks (0-10): ");
                   r2 = sc.nextFloat();
                    if (r2 > 0 && r2 < 10){
                        break;
                    }
                    else{
                        System.out.println(" Invalid Round2.");
                    }
            }

            // System.out.print("Enter Round2 Marks (0-10): ");
            // float r2 = sc.nextFloat();
            // if (r2 < 0 || r2 > 10) { System.out.println(" Invalid Round2."); return; }

            float r3 ;
            while(true){
                    System.out.print("Enter Round3 Marks (0-10): ");
                   r3 = sc.nextFloat();
                    if (r3 > 0 && r3 < 10){
                        break;
                    }
                    else{
                        System.out.println(" Invalid Round3.");
                    }
            }

            // System.out.print("Enter Round3 Marks (0-10): ");
            // float r3 = sc.nextFloat();
            // if (r3 < 0 || r3 > 10) { System.out.println(" Invalid Round3."); return; }

             float tech ;
            while(true){
                    System.out.print("Enter Technical Round Marks (0-20): ");
                   tech = sc.nextFloat();
                   if (tech > 0 && tech < 20){
                        break;
                    }
                    else{
                        System.out.println(" Invalid Technical Round.");
                    }
            }

            // System.out.print("Enter Technical Round Marks (0-20): ");
            // float tech = sc.nextFloat();
            // if (tech < 0 || tech > 20) { System.out.println(" Invalid Technical Round."); return; }

            // ---- Auto calculation + decision ----
            float total = r1 + r2 + r3 + tech; // max 50
            if (total < 0 || total > 50) { System.out.println(" Total out of range."); return; }
            // String result = (total >= 35) ? "Selected" : "Rejected";
            String result;
            if(r1 < 6.5  || r2 <6.5 || r3< 6.5 || tech <13 || total<35 ){
                result = "Rejected";
            }
            else{
                result = "Selected";
            }

            // ---- Insert (rank is filled later) ----
            String insertSql =
                "INSERT INTO candidates (StudentName, CollegeName, Round1Marks, Round2Marks, Round3Marks, " +
                "TechnicalRoundMarks, TotalMarks, Result) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pst = con.prepareStatement(insertSql)) {
                pst.setString(1, studentName);
                pst.setString(2, collegeName);
                pst.setFloat(3, r1);
                pst.setFloat(4, r2);
                pst.setFloat(5, r3);
                pst.setFloat(6, tech);
                pst.setFloat(7, total);
                pst.setString(8, result);
                pst.executeUpdate();
            }
            System.out.println(" Candidate saved.");

            // ---- Recompute ranks (dense rank: 1,1,2,3...) ----
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(
                     "SELECT id, TotalMarks FROM candidates ORDER BY TotalMarks DESC, id ASC")) {

                int currentRank = 0;
                Float prevMarks = null;

                while (rs.next()) {
                    int id = rs.getInt("id");
                    float marks = rs.getFloat("TotalMarks");

                    if (prevMarks == null || Float.compare(marks, prevMarks) != 0) {
                        currentRank++;          // new distinct marks → next rank
                        prevMarks = marks;
                    }

                    try (PreparedStatement up = con.prepareStatement(
                            "UPDATE candidates SET candidate_rank = ? WHERE id = ?")) {
                        up.setInt(1, currentRank);
                        up.setInt(2, id);
                        up.executeUpdate();
                    }
                }
            }

            // ---- Display all candidates sorted by rank ----
            String selectSql = "SELECT id, StudentName, CollegeName, Round1Marks, Round2Marks, Round3Marks, " +
                               "TechnicalRoundMarks, TotalMarks, Result, candidate_rank " +
                               "FROM candidates ORDER BY candidate_rank ASC, TotalMarks DESC, id ASC";

            try (PreparedStatement pst = con.prepareStatement(selectSql);
                 ResultSet rs = pst.executeQuery()) {

                System.out.printf("%-4s %-20s %-20s %-6s %-6s %-6s %-8s %-11s %-10s %-5s%n",
                        "ID", "StudentName", "CollegeName", "R1", "R2", "R3", "Tech", "TotalMarks", "Result", "Rank");
                System.out.println("------------------------------------------------------------------------------------------------------");

                while (rs.next()) {
                    System.out.printf("%-4d %-20s %-20s %-6.1f %-6.1f %-6.1f %-8.1f %-11.1f %-10s %-5d%n",
                            rs.getInt("id"),
                            rs.getString("StudentName"),
                            rs.getString("CollegeName"),
                            rs.getFloat("Round1Marks"),
                            rs.getFloat("Round2Marks"),
                            rs.getFloat("Round3Marks"),
                            rs.getFloat("TechnicalRoundMarks"),
                            rs.getFloat("TotalMarks"),
                            rs.getString("Result"),
                            rs.getInt("candidate_rank"));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
