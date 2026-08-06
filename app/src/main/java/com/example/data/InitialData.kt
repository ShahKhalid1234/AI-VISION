package com.example.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object InitialData {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    private fun <T> toJsonListString(list: List<T>, itemKlass: Class<T>): String {
        val type = Types.newParameterizedType(List::class.java, itemKlass)
        return moshi.adapter<List<T>>(type).toJson(list)
    }

    val concepts = listOf(
        ConceptEntity(
            id = "svd",
            title = "Singular Value Decomposition (SVD)",
            category = "Mathematics",
            shortDesc = "The mathematical foundation of dimensionality reduction, principal components, and solving overdetermined linear systems in computer vision.",
            difficulty = "Intermediate",
            intuitiveExplanationBeginner = """
                Imagine taking a 3D clay model and rotating, stretching, and squishing it onto a flat sheet of paper. SVD is a mathematical tool that breaks down any complex matrix (which is just a transformation, like a camera stretching or rotating an image) into three simple steps: 
                1. Rotate it (using a matrix called U).
                2. Stretch or compress it along certain directions (using scaling factors called Singular Values).
                3. Rotate it again (using a matrix called V).
                
                By focusing only on the largest stretching directions (the biggest singular values), we can discard the tiny ones, reducing noise and compressing the image or dataset without losing its core shape! This is used everywhere from image compression to removing camera noise.
            """.trimIndent(),
            intuitiveExplanationIntermediate = """
                For any m x n real matrix A, SVD guarantees we can decompose it into three matrices: A = U * Σ * V^T. 
                - U represents an orthogonal matrix of left singular vectors (spanning the column space).
                - V represents an orthogonal matrix of right singular vectors (spanning the row space).
                - Σ is a diagonal matrix containing the non-negative singular values σ_i in descending order.
                
                This decomposition reveals the singular spectrum of the transformation. In Computer Vision, the rank of the matrix represents the amount of independent visual information. By nulling the smallest singular values (Truncated SVD), we get the optimal low-rank approximation under the Frobenius norm (Eckart-Young-Mirsky Theorem). This is the exact math powering Principal Component Analysis (PCA) for face recognition (Eigenfaces) and camera calibration.
            """.trimIndent(),
            intuitiveExplanationAdvanced = """
                Mathematically, SVD decomposes the transformation A: R^n -> R^m into a composition of isometric rotations and anisotropic scalings. The singular values σ_j are the positive square roots of the eigenvalues of A^T A (or A A^T). 
                
                Crucially, SVD provides the most numerically stable method for computing the Pseudoinverse (Moore-Penrose) A^+ = V Σ^+ U^T, which is the foundational engine for solving overdetermined homogeneous systems Ax = 0 under the unit norm constraint ||x|| = 1. In geometric vision, this is known as Direct Linear Transformation (DLT), used to compute Homographies, Camera Projection matrices, and Epipolar Essential matrices.
            """.trimIndent(),
            formalMath = """
                Given a matrix A ∈ R^(m×n), its Singular Value Decomposition is:
                
                A = U Σ V^T
                
                Where:
                - U ∈ R^(m×m) is an orthogonal matrix (U^T U = I_m). The columns of U are the eigenvectors of A A^T.
                - V ∈ R^(n×n) is an orthogonal matrix (V^T V = I_n). The columns of V are the eigenvectors of A^T A.
                - Σ ∈ R^(m×n) is a diagonal matrix with non-negative diagonal elements σ_1 ≥ σ_2 ≥ ... ≥ σ_r > 0, called singular values, where r = rank(A).
                
                Moore-Penrose Pseudoinverse is defined as:
                A^+ = V Σ^+ U^T, where Σ^+ contains 1/σ_i for positive singular values and 0 otherwise.
            """.trimIndent(),
            proofSketch = """
                Theorem: Every real matrix A has an SVD decomposition.
                
                Proof Sketch:
                1. Consider the matrix A^T A ∈ R^(n×n). It is symmetric ( (A^T A)^T = A^T A ) and positive semidefinite ( x^T A^T A x = ||Ax||^2 ≥ 0 ).
                2. By the Spectral Theorem, there exists an orthonormal basis of eigenvectors {v_1, ..., v_n} for A^T A with eigenvalues λ_1 ≥ λ_2 ≥ ... ≥ λ_n ≥ 0.
                3. Define singular values σ_i = √λ_i. Suppose there are r non-zero singular values.
                4. For i ≤ r, define u_i = (1 / σ_i) * A * v_i. We verify that {u_1, ..., u_r} are orthonormal:
                   ⟨u_i, u_j⟩ = (1 / (σ_i σ_j)) (A v_i)^T (A v_j) = (1 / (σ_i σ_j)) v_i^T (A^T A v_j) = (λ_j / (σ_i σ_j)) v_i^T v_j. Since v_i^T v_j is 0 (i≠j) or 1 (i=j), ⟨u_i, u_j⟩ = δ_ij.
                5. Extend {u_1, ..., u_r} to an orthonormal basis of R^m, forming U. Construct V with columns v_i, and Σ with diagonal σ_i.
                6. Thus, U Σ V^T * v_i = σ_i * u_i = A * v_i. Since this holds for the basis, A = U Σ V^T. Q.E.D.
            """.trimIndent(),
            realWorldApps = """
                - **Image Compression**: High-definition images can be compressed by taking only the top-k singular values.
                - **Eigenfaces**: Face recognition via principal components in early computer vision.
                - **Direct Linear Transform (DLT)**: Solving camera matrix P in calibration and fitting homographies between images.
                - **Noise Filtering**: Eliminating weak singular values to recover clean signal fields.
            """.trimIndent(),
            pythonCode = """
                import numpy as np
                import torch
                
                # NumPy SVD and Image Compression
                def compress_image_np(image_gray, k_components):
                    U, S, Vt = np.linalg.svd(image_gray, full_matrices=False)
                    # Keep top k components
                    U_k = U[:, :k_components]
                    S_k = np.diag(S[:k_components])
                    Vt_k = Vt[:k_components, :]
                    compressed = U_k @ S_k @ Vt_k
                    return compressed
                
                # PyTorch SVD on GPU for fast reconstruction
                def solve_dlt_pytorch(A_tensor):
                    # Ax = 0, find x minimizing ||Ax|| s.t. ||x|| = 1
                    # Solution is the last column of V (or last row of V^T)
                    _, _, Vt = torch.linalg.svd(A_tensor)
                    x = Vt[-1, :] # Last row of V^T
                    return x
            """.trimIndent(),
            diagramMermaid = """
                graph LR
                    A[Matrix A] -->|SVD| U[U: Left Singular Vectors]
                    A -->|SVD| S[Sigma: Singular Values Diagonal]
                    A -->|SVD| V[V^T: Right Singular Vectors]
                    S -->|Truncate Weak Values| S_k[Top-k Sigma]
                    U & S_k & V -->|Matrix Multiply| A_k[Reconstructed Image/Matrix]
            """.trimIndent(),
            researchPapersJson = toJsonListString(listOf(
                ResearchPaper(
                    title = "Eigenfaces for Recognition",
                    authors = "Matthew Turk, Alex Pentland",
                    year = "1991",
                    importance = "First breakthrough paper using dimensionality reduction techniques derived from SVD/PCA for robust automated face recognition.",
                    url = "https://www.face-rec.org/algorithms/PCA/jocn.pdf"
                ),
                ResearchPaper(
                    title = "Singular Value Analysis of Images",
                    authors = "Harry C. Andrews, Claude L. Patterson",
                    year = "1976",
                    importance = "Seminal work demonstrating SVD's capability in image compression, restoration, and digital image processing.",
                    url = "https://ieeexplore.ieee.org/document/1454199"
                )
            ), ResearchPaper::class.java),
            quizJson = toJsonListString(listOf(
                QuizQuestion(
                    id = 1,
                    question = "In the SVD of a matrix A (A = UΣV^T), what do the columns of V represent?",
                    options = listOf(
                        "Eigenvectors of AA^T",
                        "Eigenvectors of A^T A",
                        "Singular values",
                        "Projection residuals"
                    ),
                    correctOptionIndex = 1,
                    explanation = "The columns of V are the eigenvectors of A^T A, while the columns of U are the eigenvectors of AA^T."
                ),
                QuizQuestion(
                    id = 2,
                    question = "When solving the homogeneous system Ax = 0 using SVD under the constraint ||x|| = 1, which vector is the optimal solution?",
                    options = listOf(
                        "The left singular vector corresponding to the largest singular value",
                        "The right singular vector corresponding to the largest singular value",
                        "The right singular vector corresponding to the smallest singular value",
                        "The average of all diagonal entries of Sigma"
                    ),
                    correctOptionIndex = 2,
                    explanation = "The unit vector x that minimizes ||Ax||^2 is the right singular vector corresponding to the smallest singular value (the last column of V)."
                )
            ), QuizQuestion::class.java),
            interviewQuestionsJson = toJsonListString(listOf(
                InterviewQuestion(
                    question = "Why is SVD preferred over simple eigendecomposition for camera calibration and homography estimation?",
                    answer = "Eigendecomposition requires a square matrix. Camera calibration matrices and DLT coefficient matrices are generally non-square (m x n rectangular). SVD applies directly to non-square matrices and is highly numerically stable against noise and poorly conditioned systems."
                ),
                InterviewQuestion(
                    question = "How does truncated SVD act as a low-pass noise filter on raw matrices?",
                    answer = "In a noisy matrix, the true structural data aligns with the directions of maximum variance (large singular values), while high-frequency random noise disperses evenly and occupies the directions of minimal variance (small singular values). Zeroing out small singular values reconstructs the matrix without the high-frequency noise components."
                )
            ), InterviewQuestion::class.java),
            xPos = 250f,
            yPos = 180f
        ),
        ConceptEntity(
            id = "fourier",
            title = "2D Discrete Fourier Transform",
            category = "Signal Processing",
            shortDesc = "The decomposition of spatial images into their constituent 2D spatial frequencies, bridging spatial filters and frequency domain optimization.",
            difficulty = "Advanced",
            intuitiveExplanationBeginner = """
                Imagine looking at an ocean. Instead of measuring the height of every single wave at every point, you could describe the ocean by saying 'there is a huge slow wave rolling from the west, plus a fast little ripple going north.' 
                
                The 2D Fourier Transform does this for images! An image is just a landscape of bright and dark 'waves'. The Fourier Transform converts your image from pixels (spatial domain) into a map of frequencies (frequency domain). The center of this map shows big, slow gradients (like skies and shadows), while the outer edges show fast, sharp changes (like edges, texture, and fine noise).
            """.trimIndent(),
            intuitiveExplanationIntermediate = """
                The 2D Discrete Fourier Transform (DFT) converts an image f(x,y) into a complex frequency representation F(u,v). The magnitude spectrum |F(u,v)| represents the strength of horizontal and vertical sinusoidal waves at frequency (u,v), while the phase spectrum ∠F(u,v)| captures the alignment of these waves.
                
                In Computer Vision, convolving an image with a filter in the spatial domain is mathematically equivalent to element-wise multiplication in the frequency domain (Convolution Theorem). For large filter kernels (e.g. large Gaussian blur), convolving via Fast Fourier Transform (FFT) is drastically faster, reducing complexity from O(N^2 * K^2) to O(N^2 log N).
            """.trimIndent(),
            intuitiveExplanationAdvanced = """
                Formally, the 2D DFT projects the image signal onto an orthonormal basis of 2D complex exponentials. It is a unitary transform, meaning it preserves energy (Parseval's Theorem). 
                
                A key concept is the duality of properties: sharp spatial edges correspond to wide frequency distributions, and smooth Gaussian spatial filters correspond to narrow Gaussian low-pass filters in the frequency domain. Phase information is critically important; swapping the phase of two images while keeping their magnitudes reconstructs the spatial structures of the image from which the phase was taken, proving that structural boundaries are encoded almost entirely in phase synchronization.
            """.trimIndent(),
            formalMath = """
                For an M × N image f(x, y), the 2D DFT F(u, v) is defined as:
                
                F(u, v) = ∑_(x=0)^(M-1) ∑_(y=0)^(N-1) f(x, y) e^(-j 2π (ux/M + vy/N))
                
                Where j = √-1. The Inverse 2D DFT is:
                f(x, y) = (1 / MN) ∑_(u=0)^(M-1) ∑_(v=0)^(N-1) F(u, v) e^(j 2π (ux/M + vy/N))
                
                The Convolution Theorem states:
                f(x, y) * h(x, y) ⟺ F(u, v) · H(u, v)
                Where * denotes 2D spatial convolution and · is point-wise multiplication.
            """.trimIndent(),
            proofSketch = """
                Theorem: Convolution in the spatial domain equals element-wise multiplication in the frequency domain (1D version shown for clarity, scales directly to 2D).
                
                Proof Sketch:
                Let y(n) = (x * h)(n) = ∑_m x(m)h(n-m). Taking the DFT of y(n):
                Y(k) = ∑_n [ ∑_m x(m)h(n-m) ] e^(-j 2π k n / N)
                
                Rearranging the summation order:
                Y(k) = ∑_m x(m) [ ∑_n h(n-m) e^(-j 2π k n / N) ]
                
                Let l = n - m, then n = l + m. As n ranges from 0 to N-1, l also covers a full period due to circular boundary assumption:
                Y(k) = ∑_m x(m) [ ∑_l h(l) e^(-j 2π k (l+m) / N) ]
                Y(k) = ∑_m x(m) e^(-j 2π k m / N) [ ∑_l h(l) e^(-j 2π k l / N) ]
                
                The left summation is exactly the DFT X(k), and the right is the DFT H(k).
                Y(k) = X(k) · H(k). Q.E.D.
            """.trimIndent(),
            realWorldApps = """
                - **Fast Image Convolution**: Utilizing FFT for running large kernels (e.g. Motion Blur, High-radius Gaussian filter).
                - **Periodic Noise Removal**: Filtering out spikes in frequency magnitude using notch filters (e.g. scanning artifacts).
                - **Phase Correlation**: Image registration, alignment, and optical flow estimation by finding the phase shift.
                - **JPEG Compression**: Based on Discrete Cosine Transform (DCT), a close cousin of DFT which avoids complex imaginary components.
            """.trimIndent(),
            pythonCode = """
                import cv2
                import numpy as np
                
                # Compute 2D FFT and display magnitude spectrum
                def compute_fft_spectrum(img_gray):
                    # Compute FFT
                    f_transform = np.fft.fft2(img_gray)
                    # Shift zero-frequency component to the center
                    f_shift = np.fft.fftshift(f_transform)
                    
                    # Compute magnitude spectrum on log scale
                    magnitude_spectrum = 20 * np.log(np.abs(f_shift) + 1e-5)
                    return magnitude_spectrum
                
                # Apply Low Pass Filter in Frequency Domain
                def apply_low_pass(img_gray, radius=30):
                    f_transform = np.fft.fft2(img_gray)
                    f_shift = np.fft.fftshift(f_transform)
                    
                    rows, cols = img_gray.shape
                    crow, ccol = rows // 2, cols // 2
                    
                    # Create circular low-pass mask
                    mask = np.zeros((rows, cols), np.uint8)
                    y, x = np.ogrid[-crow:rows-crow, -ccol:cols-ccol]
                    mask_area = x*x + y*y <= radius*radius
                    mask[mask_area] = 1
                    
                    # Filter signal
                    f_shift_filtered = f_shift * mask
                    # Inverse Shift and Inverse FFT
                    f_ishift = np.fft.ifftshift(f_shift_filtered)
                    img_back = np.fft.ifft2(f_ishift)
                    img_back = np.abs(img_back)
                    
                    return np.uint8(np.clip(img_back, 0, 255))
            """.trimIndent(),
            diagramMermaid = """
                graph LR
                    Img[Spatial Image] -->|2D FFT| FFT[Complex Frequencies]
                    FFT -->|Shift| Shifted[Zero-Freq Centered]
                    Shifted -->|Apply Mask| Mask[Low/High/Notch Filter]
                    Mask -->|Inverse Shift| IShift[Filtered Complex]
                    IShift -->|2D IFFT| Recon[Spatially Filtered Image]
            """.trimIndent(),
            researchPapersJson = toJsonListString(listOf(
                ResearchPaper(
                    title = "An Algorithm for the Machine Calculation of Complex Fourier Series",
                    authors = "James W. Cooley, John W. Tukey",
                    year = "1965",
                    importance = "Introduced the Fast Fourier Transform (FFT), reducing DFT complexity from O(N^2) to O(N log N). The software foundation of all modern digital signal and image processing.",
                    url = "https://www.ams.org/journals/mcom/1965-19-090/S0025-5718-1965-0178586-1/S0025-5718-1965-0178586-1.pdf"
                )
            ), ResearchPaper::class.java),
            quizJson = toJsonListString(listOf(
                QuizQuestion(
                    id = 1,
                    question = "Why does the center of a shifted 2D Fourier transform represent low frequencies?",
                    options = listOf(
                        "Because high frequencies cancel out at the center.",
                        "Standard convention shifts the zero-frequency (DC component) to the coordinate (M/2, N/2).",
                        "The corners naturally contain low-frequency illumination.",
                        "Because of circular aliasing."
                    ),
                    correctOptionIndex = 1,
                    explanation = "The standard DFT places the zero-frequency DC component at the top-left (0,0). For visual analysis, we perform an fftshift which rearranges the quadrants to center zero frequencies at (M/2, N/2)."
                ),
                QuizQuestion(
                    id = 2,
                    question = "If an image is blurred using a very wide spatial Gaussian kernel, what happens to its frequency spectrum?",
                    options = listOf(
                        "The frequency spectrum expands, retaining more high-frequency coefficients.",
                        "The frequency spectrum shrinks, attenuating high-frequency components (low-pass filter).",
                        "The phase spectrum is inverted while magnitude remains constant.",
                        "The spectrum is unaffected because blurring is a spatial-only operation."
                    ),
                    correctOptionIndex = 1,
                    explanation = "Blurring is a low-pass filtering operation. A wide spatial Gaussian corresponds to a narrow frequency Gaussian, which blocks (attenuates) high-frequency details like edges and noise."
                )
            ), QuizQuestion::class.java),
            interviewQuestionsJson = toJsonListString(listOf(
                InterviewQuestion(
                    question = "What is the computational saving of performing 2D convolution of an N x N image with a K x K filter in the frequency domain vs. the spatial domain?",
                    answer = "Spatial convolution takes O(N^2 * K^2) multiplications. Frequency domain convolution takes O(N^2 log N) for the forward FFTs, O(N^2) for element-wise multiplication, and O(N^2 log N) for inverse FFT. Therefore, when the filter size K is large (specifically K > sqrt(log N)), performing convolution via FFT in the frequency domain is significantly faster."
                ),
                InterviewQuestion(
                    question = "What happens if you reconstruct an image using the magnitude spectrum of Image A and the phase spectrum of Image B?",
                    answer = "The reconstructed image will structurally resemble Image B. This is because phase contains the spatial localization information (where the waves line up to construct edges and objects), whereas the magnitude spectrum only contains global frequency intensities (overall textures and lighting)."
                )
            ), InterviewQuestion::class.java),
            xPos = 450f,
            yPos = 180f
        ),
        ConceptEntity(
            id = "sobel",
            title = "Sobel Edge Detection & Gradients",
            category = "Classical CV",
            shortDesc = "The mathematical basis for estimating local spatial image derivatives, critical for edge detection, feature descriptors, and early vision.",
            difficulty = "Beginner",
            intuitiveExplanationBeginner = """
                How does a computer see an 'edge'? It looks for places where the color changes suddenly—like a black cat sitting in front of a white wall.
                
                The Sobel filter is like a digital magnifying glass that highlights these rapid color changes. It slides a tiny 3x3 grid of numbers over every pixel in the image. This grid measures how much brighter or darker the neighbors are. By doing this horizontally and vertically, it calculates the 'gradient'—which way the color is changing and how fast. This is how computer vision systems map out outlines and shapes!
            """.trimIndent(),
            intuitiveExplanationIntermediate = """
                The Sobel operator is a discrete differentiation operator computing an approximation of the gradient of the image intensity function. By convolving the image with two 3x3 kernels, we obtain the horizontal gradient (G_x) and vertical gradient (G_y).
                
                Unlike a simple central difference, the Sobel filter combines differentiation with Gaussian-like smoothing (the factors of 2 in the kernels). This smoothing makes the derivative calculation highly robust to high-frequency sensor noise. Gradients calculated by Sobel are the building blocks of classic hand-crafted descriptors like HOG (Histogram of Oriented Gradients) and SIFT.
            """.trimIndent(),
            intuitiveExplanationAdvanced = """
                Formally, the Sobel operator approximates the spatial gradient of a continuous 2D intensity function I(x,y). The gradient vector ∇I = [∂I/∂x, ∂I/∂y]^T points in the direction of maximum intensity change.
                
                The 3x3 Sobel kernels are separable. For instance, the horizontal kernel G_x can be factored as:
                G_x = [1, 2, 1]^T * [-1, 0, 1]
                This factorization mathematically proves that Sobel is a composition of a horizontal central derivative operator and a vertical smoothing filter. Separability allows us to compute the 2D convolution as two 1D passes, reducing computations per pixel from O(K^2) to O(K) (from 9 multiplications to 6 per pixel).
            """.trimIndent(),
            formalMath = """
                The Sobel kernels for an image I are:
                
                G_x = [[-1, 0, 1], [-2, 0, 2], [-1, 0, 1]] * I
                G_y = [[-1, -2, -1], [0, 0, 0], [1, 2, 1]] * I
                
                For each pixel (x,y), the gradient magnitude is:
                M(x, y) = √(G_x(x, y)^2 + G_y(x, y)^2)  ≈ |G_x(x, y)| + |G_y(x, y)|
                
                The gradient direction (orientation angle) is:
                θ(x, y) = atan2(G_y(x, y), G_x(x, y))
            """.trimIndent(),
            proofSketch = """
                Theorem: The 3x3 Sobel kernel G_x is separable and represents a 1D central difference smoothed by a binomial filter.
                
                Proof Sketch:
                1. Consider the vertical binomial (smoothing) vector: s = [1, 2, 1]^T.
                2. Consider the horizontal central difference (derivative) vector: d = [-1, 0, 1].
                3. Compute the outer product of these two vectors:
                   s * d = [1, 2, 1]^T * [-1, 0, 1]
                         = [[1*-1, 1*0, 1*1],
                            [2*-1, 2*0, 2*1],
                            [1*-1, 1*0, 1*1]]
                         = [[-1, 0, 1],
                            [-2, 0, 2],
                            [-1, 0, 1]]
                4. This is exactly the horizontal Sobel kernel G_x. By symmetry, G_y = d^T * s^T.
                5. Since the 2D kernel is an outer product of two 1D kernels, 2D convolution can be separated into two consecutive 1D convolving operations. Q.E.D.
            """.trimIndent(),
            realWorldApps = """
                - **Edge Detection**: Initial step for finding outlines (e.g., Canny edge detector uses Sobel gradients internally).
                - **SIFT / HOG Feature Extraction**: Calculating gradient orientation histograms to represent object shapes.
                - **Image Sharpness Metric**: Calculating the variance of Sobel gradients to determine if a camera is in focus.
                - **Saliency Maps**: Finding high-contrast regions that draw human visual attention.
            """.trimIndent(),
            pythonCode = """
                import cv2
                import numpy as np
                
                def compute_sobel_gradients(img_gray):
                    # Compute horizontal gradient (CV_64F prevents clipping negative gradients)
                    sobel_x = cv2.Sobel(img_gray, cv2.CV_64F, 1, 0, ksize=3)
                    # Compute vertical gradient
                    sobel_y = cv2.Sobel(img_gray, cv2.CV_64F, 0, 1, ksize=3)
                    
                    # Compute gradient magnitude
                    magnitude = np.sqrt(sobel_x**2 + sobel_y**2)
                    # Scale to 0-255
                    magnitude_scaled = np.uint8(np.clip(magnitude, 0, 255))
                    
                    # Compute orientation
                    orientation = np.arctan2(sobel_y, sobel_x)
                    return magnitude_scaled, orientation
            """.trimIndent(),
            diagramMermaid = """
                graph TD
                    Input[Grayscale Image] -->|Convolve G_x| Gx[Horizontal Derivative]
                    Input -->|Convolve G_y| Gy[Vertical Derivative]
                    Gx & Gy -->|Square & Sum| SquareSum[Gx^2 + Gy^2]
                    SquareSum -->|Sqrt| Mag[Gradient Magnitude]
                    Gx & Gy -->|Atan2| Dir[Gradient Orientation θ]
            """.trimIndent(),
            researchPapersJson = toJsonListString(listOf(
                ResearchPaper(
                    title = "An Isotropic 3x3 Image Gradient Operator",
                    authors = "Irwin Sobel, Gary Feldman",
                    year = "1968",
                    importance = "The original presentation of the Sobel edge detection operator at the Stanford Artificial Intelligence Project, establishing standard neighborhood derivative operators.",
                    url = "https://www.researchgate.net/publication/285383389_An_Isotropic_3x3_Image_Gradient_Operator"
                )
            ), ResearchPaper::class.java),
            quizJson = toJsonListString(listOf(
                QuizQuestion(
                    id = 1,
                    question = "Why does the Sobel filter use values of 2 in the center of its derivative kernel instead of 1?",
                    options = listOf(
                        "To multiply the edge response to make it look brighter.",
                        "To provide isotropic smoothing, giving more weight to the central neighbor and reducing noise sensitivity.",
                        "Because of hardware limitations in 1968.",
                        "To compute diagonal gradients instead of horizontal."
                    ),
                    correctOptionIndex = 1,
                    explanation = "The central weight of 2 aligns with a binomial filter approximation of a Gaussian kernel, which reduces noise sensitivity and makes the derivative estimation more isotropic (uniform across directions)."
                ),
                QuizQuestion(
                    id = 2,
                    question = "What is the primary benefit of convolving with a separable filter?",
                    options = listOf(
                        "It eliminates the need for floating-point calculations.",
                        "It changes 2D convolution complexity from O(K^2) multiplications per pixel to O(K).",
                        "It automatically normalizes the image illumination.",
                        "It allows the filter to be processed in the frequency domain without FFT."
                    ),
                    correctOptionIndex = 1,
                    explanation = "Separability factors a KxK kernel into Kx1 and 1xK kernels. This drops the per-pixel calculation complexity from K^2 multiplications to 2K, which speeds up processing significantly."
                )
            ), QuizQuestion::class.java),
            interviewQuestionsJson = toJsonListString(listOf(
                InterviewQuestion(
                    question = "If an image contains strong salt-and-pepper noise, what is the prerequisite step before applying Sobel?",
                    answer = "You must apply a Median filter first. Sobel gradients are high-pass filters that amplify high-frequency noise. Applying Sobel directly to salt-and-pepper noise would result in hundreds of false-positive point edges. A median filter is highly effective at removing impulse noise while preserving sharp boundaries."
                ),
                InterviewQuestion(
                    question = "Why do we use float64 (or CV_64F) instead of uint8 when calculating gradients in OpenCV?",
                    answer = "Gradients represent differences in brightness. Moving from bright to dark produces negative values (e.g., 50 - 200 = -150). Standard uint8 values are clipped at [0, 255], meaning all negative derivatives would be clipped to 0, losing half of the edge boundaries (transitions from light to dark). Floating-point formats preserve negative signs, which we later convert back via absolute value."
                )
            ), InterviewQuestion::class.java),
            xPos = 350f,
            yPos = 350f
        ),
        ConceptEntity(
            id = "backprop",
            title = "Backpropagation & Gradient Descent",
            category = "Optimization",
            shortDesc = "The optimization engine of modern deep learning. How neural networks calculate gradients via the multivariable calculus chain rule.",
            difficulty = "Intermediate",
            intuitiveExplanationBeginner = """
                Imagine trying to hit a target with a bow and arrow while blindfolded. Your friend looks at where your arrow lands and tells you: 'You shot 2 feet too high and 1 foot to the left.' 
                
                You use that feedback to adjust your stance. 
                In a neural network, the 'arrow' is the prediction, the 'friend' is the Loss Function (measuring the error), and the adjustments are made by Backpropagation. It calculates exactly how much each mathematical dial (or weight) inside the brain contributed to the final error, then turns those dials in the direction that reduces the error (Gradient Descent). It does this by working backwards, step-by-step, from the output to the input!
            """.trimIndent(),
            intuitiveExplanationIntermediate = """
                Backpropagation is the systemic application of the chain rule of calculus to compute the partial derivatives of a scalar loss function L with respect to all trainable weights W in a computational graph.
                
                During the forward pass, the network computes values sequentially and stores activation states. During the backward pass, the error gradient is propagated in reverse. For any node z = f(x, y), if we receive the incoming gradient dL/dz from the top, we compute local gradients dz/dx and dz/dy, then multiply them to get dL/dx and dL/dy. This backward propagation of local Jacobians prevents redundant computations, achieving O(N) complexity where N is the number of operations.
            """.trimIndent(),
            intuitiveExplanationAdvanced = """
                Formally, backpropagation computes the gradient of a loss function L: R^D -> R on a directed acyclic graph (DAG). 
                
                Let z_i be the activation of node i. For any child node z_j that depends on z_i, we apply the multivariable chain rule:
                ∂L / ∂z_i = ∑_(j ∈ Children(i)) (∂L / ∂z_j) * (∂z_j / ∂z_i)
                
                In matrix notation, for a fully connected layer y = W x + b, the gradients are:
                ∂L / ∂x = W^T (∂L / ∂y)
                Reflecting that the backward pass projects the error gradient through the transpose of the weight matrix. Vanishing gradients occur when consecutive local Jacobians have singular values strictly less than 1, causing the propagated gradient to decay exponentially with depth.
            """.trimIndent(),
            formalMath = """
                Given a computational path w -> z -> y -> L, the chain rule states:
                
                ∂L / ∂w = (∂L / ∂y) · (∂y / ∂z) · (∂z / ∂w)
                
                For a linear layer y = W x + b and loss L, let δ = ∂L/∂y:
                - ∂L / ∂W = δ · x^T  (Outer product of gradient vector and input vector)
                - ∂L / ∂b = δ
                - ∂L / ∂x = W^T · δ
                
                The gradient descent update rule for learning rate η is:
                W^(t+1) = W^(t) - η · (∂L / ∂W)
            """.trimIndent(),
            proofSketch = """
                Theorem: The multivariable chain rule correctly calculates the total derivative of a composite function.
                
                Proof Sketch:
                Let f: R^n -> R^m and g: R^m -> R. Let y = f(x) and z = g(y).
                An infinitesimal change dx in x produces a change dy in y, approximated by the Jacobian matrix J_f(x):
                dy ≈ J_f(x) * dx  => dy_i ≈ ∑_j (∂y_i/∂x_j) * dx_j
                
                The change in final scalar z is approximated by the gradient ∇g(y):
                dz ≈ ∇g(y)^T * dy = ∑_i (∂z/∂y_i) * dy_i
                
                Substituting dy_i into the expression for dz:
                dz ≈ ∑_i (∂z/∂y_i) [ ∑_j (∂y_i/∂x_j) * dx_j ]
                dz ≈ ∑_j [ ∑_i (∂z/∂y_i) * (∂y_i/∂x_j) ] dx_j
                
                Since dz ≈ ∇_x z^T * dx, the partial derivative is:
                ∂z / ∂x_j = ∑_i (∂z / ∂y_i) * (∂y_i / ∂x_j)
                In matrix form: J_(g◦f)(x) = J_g(y) * J_f(x). Q.E.D.
            """.trimIndent(),
            realWorldApps = """
                - **Deep Neural Network Training**: Training MLPs, CNNs, Transformers, and RNNs.
                - **Autodiff Engines**: Core engine in PyTorch (Autograd), TensorFlow, and JAX.
                - **Adversarial Attacks**: Generating adversarial images (FGSM) by propagating loss gradients back to the image pixels.
                - **Neural Style Transfer**: Optimizing input image pixels to match style and content loss targets.
            """.trimIndent(),
            pythonCode = """
                import numpy as np
                
                # Manual backward pass for a single linear layer with ReLU activation
                class ManualLinearReLU:
                    def __init__(self, in_features, out_features):
                        # Initialize weights
                        self.W = np.random.randn(out_features, in_features) * 0.01
                        self.b = np.zeros((out_features, 1))
                        self.x = None
                        self.z = None
                        self.a = None
                        
                    def forward(self, x):
                        # x shape: (in_features, batch_size)
                        self.x = x
                        self.z = np.dot(self.W, x) + self.b
                        self.a = np.maximum(0, self.z) # ReLU
                        return self.a
                        
                    def backward(self, da_incoming, learning_rate=0.01):
                        # da_incoming is dL/da: (out_features, batch_size)
                        # 1. Backprop through ReLU: dz = da * I(z > 0)
                        dz = da_incoming * (self.z > 0)
                        
                        # 2. Compute gradients with respect to parameters
                        batch_size = self.x.shape[1]
                        dW = np.dot(dz, self.x.T) / batch_size
                        db = np.sum(dz, axis=1, keepdims=True) / batch_size
                        
                        # 3. Compute gradient with respect to inputs (to pass backward)
                        dx_outgoing = np.dot(self.W.T, dz)
                        
                        # 4. Gradient Descent Update
                        self.W -= learning_rate * dW
                        self.b -= learning_rate * db
                        
                        return dx_outgoing
            """.trimIndent(),
            diagramMermaid = """
                graph LR
                    X[Input x] -->|Forward: W*x+b| Z[Pre-Activation z]
                    Z -->|Forward: ReLU| A[Activation a]
                    A -->|Forward: Loss| L[Loss L]
                    L -->|Backward: dL/da| A
                    A -->|Backward: * I z>0| Z
                    Z -->|Backward: * x^T| dW[Grad dW]
                    Z -->|Backward: * W^T| dX[Input Grad dx]
            """.trimIndent(),
            researchPapersJson = toJsonListString(listOf(
                ResearchPaper(
                    title = "Learning Representations by Back-Propagating Errors",
                    authors = "David E. Rumelhart, Geoffrey E. Hinton, Ronald J. Williams",
                    year = "1986",
                    importance = "The landmark paper that popularized backpropagation, demonstrating that neural networks can learn internal representations of data.",
                    url = "https://www.nature.com/articles/323533a0"
                )
            ), ResearchPaper::class.java),
            quizJson = toJsonListString(listOf(
                QuizQuestion(
                    id = 1,
                    question = "Why are activation values from the forward pass stored during training?",
                    options = listOf(
                        "To display them on training dashboards.",
                        "Because the backward gradient formulas require these activation values (like x in dW = dz * x^T).",
                        "To speed up GPU inference speed.",
                        "To prevent overfitting."
                    ),
                    correctOptionIndex = 1,
                    explanation = "Calculating parameter gradients (like dW = dz · x^T) requires the activation value (x) from the forward pass. This requires keeping intermediate activations in memory until the backward pass is complete."
                ),
                QuizQuestion(
                    id = 2,
                    question = "Which activation function is most vulnerable to vanishing gradients when stacked deeply?",
                    options = listOf(
                        "ReLU",
                        "Sigmoid",
                        "LeakyReLU",
                        "GeLU"
                    ),
                    correctOptionIndex = 1,
                    explanation = "The Sigmoid function's derivative peaks at only 0.25. When chain-multiplying sigmoid derivatives in deep networks, the gradients vanish rapidly, decaying to near zero."
                )
            ), QuizQuestion::class.java),
            interviewQuestionsJson = toJsonListString(listOf(
                InterviewQuestion(
                    question = "What is the difference between backpropagation and gradient descent?",
                    answer = "Backpropagation is the algorithm that computes the partial derivatives (gradients) of the loss function with respect to each weight using the chain rule. Gradient Descent is the separate optimization algorithm that actually uses these computed gradients to update the weights in order to minimize the loss."
                ),
                InterviewQuestion(
                    question = "Explain the exploding gradient problem and how to mitigate it.",
                    answer = "Exploding gradients occur when the derivatives of the activations are large, causing the propagated gradients to grow exponentially as they travel backward. This causes chaotic updates, numerical overflow (NaN), and training instability. Mitigations include: Gradient Clipping (capping the norm of gradients), Batch Normalization, and using residual connections (ResNet)."
                )
            ), InterviewQuestion::class.java),
            xPos = 150f,
            yPos = 500f
        ),
        ConceptEntity(
            id = "cnn",
            title = "Convolutional Neural Network (CNN) Foundations",
            category = "Deep Learning",
            shortDesc = "The core architecture of computer vision models. How translation equivariance, receptive fields, and weight sharing revolutionized visual recognition.",
            difficulty = "Beginner",
            intuitiveExplanationBeginner = """
                Imagine trying to find a face in an image. If you had a standard neural network, you would have to learn what an 'eye' looks like in the top-left corner, and then learn it ALL over again for the bottom-right corner! That's incredibly wasteful.
                
                CNNs solve this using 'weight sharing'. They take a tiny grid of numbers (a filter) and slide it across the entire image—just like the Sobel filter! This filter acts as a feature detector. If it detects an eye, it will find it anywhere in the image. By stacking these layers, the network learns to detect simple lines in the first layer, shapes in the middle layers, and complex objects (like faces or cars) in the deep layers!
            """.trimIndent(),
            intuitiveExplanationIntermediate = """
                CNNs utilize three core architectural principles: Local Receptive Fields, Shared Weights, and Spatial Subsampling (Pooling).
                
                1. **Local Receptive Fields**: Neurons only connect to a small local patch of the input. This captures local spatial correlations (like edges/textures).
                2. **Shared Weights**: The same filter weights are convolved across the entire input grid. This enforces Translation Equivariance (if an edge shifts, the feature map shifts identically) and dramatically reduces parameter volume.
                3. **Pooling**: Max or Average pooling reduces spatial resolution, increasing the effective receptive field of subsequent layers so deeper neurons see larger portions of the image.
            """.trimIndent(),
            intuitiveExplanationAdvanced = """
                Formally, a 2D convolution layer map is defined by a cross-correlation operation. If W is a filter of size (C_in, K, K) and X is the input of size (C_in, H, W), the output feature map is:
                Y[c_out, i, j] = ∑_(c_in) ∑_(m) ∑_(n) X[c_in, i+m, j+n] · W[c_out, c_in, m, n] + b[c_out]
                
                The mathematical beauty of CNNs lies in their inductive bias: translation equivariance. A translation operator T_g shifts an image. Since convolution is a linear translation-equivariant operator, convolving a shifted image yields the same result as shifting the convolved image:
                (T_g X) * W = T_g (X * W)
                This makes CNNs highly sample-efficient compared to Fully Connected networks, which have no spatial layout awareness and must learn translation invariance purely through extensive data exposure.
            """.trimIndent(),
            formalMath = """
                Let X ∈ R^(C_in × H × W) be input, W ∈ R^(C_out × C_in × K × K) be weight.
                For stride s and padding p, the output spatial dimensions are:
                
                H_out = ⌊ (H - K + 2p) / s ⌋ + 1
                W_out = ⌊ (W - K + 2p) / s ⌋ + 1
                
                Max-Pooling operation over window size P x P:
                Y[c, i, j] = max_(0 ≤ m, n < P) X[c, i·s + m, j·s + n]
                
                Receptive Field (RF) of layer L is:
                RF_L = RF_(L-1) + (K_L - 1) · Jump_(L-1)
                Where Jump_L = Jump_(L-1) · Stride_L
            """.trimIndent(),
            proofSketch = """
                Theorem: Discrete convolution commutes with spatial translation (Translation Equivariance).
                
                Proof Sketch:
                Let (T_k x)[n] = x[n - k] represent a translation operator shifting a 1D signal by k steps.
                Let y[n] = (x * w)[n] = ∑_m x[m] w[n - m] represent discrete convolution.
                
                Now, convolve the shifted signal T_k x with filter w:
                ((T_k x) * w)[n] = ∑_m (T_k x)[m] w[n - m]
                                 = ∑_m x[m - k] w[n - m]
                
                Change variables: let l = m - k, then m = l + k:
                ((T_k x) * w)[n] = ∑_l x[l] w[n - (l + k)]
                                 = ∑_l x[l] w[(n - k) - l]
                
                This summation is exactly the definition of the original convolution evaluated at coordinate (n - k):
                ((T_k x) * w)[n] = y[n - k] = (T_k (x * w))[n].
                
                Thus, convolving a shifted signal is identical to convolving first and then shifting the output. Q.E.D.
            """.trimIndent(),
            realWorldApps = """
                - **Image Classification**: Foundational backbone for ResNet, EfficientNet, MobileNet.
                - **Object Detection**: CNN backbones extract features for bounding box predictors in YOLO and Faster R-CNN.
                - **Semantic Segmentation**: Fully Convolutional Networks (U-Net) classifying every pixel in medical scans.
                - **Autonomous Driving**: Processing surround-camera feeds to detect lanes, pedestrians, and signals.
            """.trimIndent(),
            pythonCode = """
                import torch
                import torch.nn as nn
                
                # Custom PyTorch CNN block with manual dimensions calculation
                class VisionBlock(nn.Module):
                    def __init__(self, in_channels, out_channels, kernel_size=3, padding=1):
                        super().__init__()
                        self.conv = nn.Conv2d(
                            in_channels=in_channels,
                            out_channels=out_channels,
                            kernel_size=kernel_size,
                            stride=1,
                            padding=padding,
                            bias=True
                        )
                        self.bn = nn.BatchNorm2d(out_channels)
                        self.relu = nn.ReLU(inplace=True)
                        self.pool = nn.MaxPool2d(kernel_size=2, stride=2)
                        
                    def forward(self, x):
                        # Shape: [B, C_in, H, W]
                        x = self.conv(x)
                        x = self.bn(x)
                        x = self.relu(x)
                        x = self.pool(x)
                        # Shape: [B, C_out, H/2, W/2]
                        return x
            """.trimIndent(),
            diagramMermaid = """
                graph LR
                    Input[Input Image HxWxC] -->|Conv 3x3| Feat[Feature Maps]
                    Feat -->|BatchNorm| Norm[Normalized Maps]
                    Norm -->|ReLU| Act[Activated Maps]
                    Act -->|Max Pool 2x2| Down[Pooled Maps H/2 x W/2]
            """.trimIndent(),
            researchPapersJson = toJsonListString(listOf(
                ResearchPaper(
                    title = "Gradient-Based Learning Applied to Document Recognition",
                    authors = "Yann LeCun, Léon Bottou, Yoshua Bengio, Patrick Haffner",
                    year = "1998",
                    importance = "Introduced LeNet-5, establishing the standard framework of modern CNNs containing alternating convolutions, pooling, and backpropagation optimization.",
                    url = "http://vision.stanford.edu/cs59s/papers/lecun_98.pdf"
                ),
                ResearchPaper(
                    title = "ImageNet Classification with Deep Convolutional Neural Networks",
                    authors = "Alex Krizhevsky, Ilya Sutskever, Geoffrey E. Hinton",
                    year = "2012",
                    importance = "AlexNet: The breakthrough paper that won the ImageNet challenge by a huge margin, kickstarting the modern Deep Learning revolution.",
                    url = "https://proceedings.neurips.cc/paper/2012/file/c3982bc38a49d588934fe7361196bef7-Paper.pdf"
                )
            ), ResearchPaper::class.java),
            quizJson = toJsonListString(listOf(
                QuizQuestion(
                    id = 1,
                    question = "Why are CNNs much more parameter-efficient than Fully Connected (Dense) networks for images?",
                    options = listOf(
                        "Because they use floating-point precision reduction.",
                        "Because they employ weight sharing and local receptive fields, decoupling parameters from input image resolution.",
                        "Because CNNs do not require backpropagation during training.",
                        "Because they only analyze the boundaries of objects."
                    ),
                    correctOptionIndex = 1,
                    explanation = "A fully connected layer connects every pixel to every neuron, leading to millions of parameters. CNNs share a small filter (e.g. 3x3) across the entire image, keeping parameters independent of input resolution."
                ),
                QuizQuestion(
                    id = 2,
                    question = "If an input image is 32x32, and you apply a 5x5 convolution with padding=0 and stride=1, what are the spatial dimensions of the output feature map?",
                    options = listOf(
                        "32x32",
                        "28x28",
                        "30x30",
                        "27x27"
                    ),
                    correctOptionIndex = 1,
                    explanation = "Using the formula: H_out = (H - K + 2P)/S + 1. Here, H_out = (32 - 5 + 0)/1 + 1 = 27 + 1 = 28. The output is 28x28."
                )
            ), QuizQuestion::class.java),
            interviewQuestionsJson = toJsonListString(listOf(
                InterviewQuestion(
                    question = "What is the difference between Translation Equivariance and Translation Invariance in CNNs?",
                    answer = "Translation Equivariance means if the input shifts, the activation shifts by the same amount (handled by Conv layers). Translation Invariance means the network output remains unchanged even if the input shifts (e.g., classifying a cat as 'cat' regardless of where it is in the frame). This is achieved through Pooling and Fully Connected layers aggregating spatial features."
                ),
                InterviewQuestion(
                    question = "How does Dilated (Atrous) Convolution increase receptive fields without increasing parameters?",
                    answer = "Dilated convolution introduces spaces ('holes') between kernel elements. A kernel with dilation rate d has spacing of d-1 between its cells. This allows the filter to cover a much wider spatial receptive field (e.g., a 3x3 filter with dilation 2 covers a 5x5 area) without adding any extra weight parameters, which is extremely useful in semantic segmentation to preserve resolution."
                )
            ), InterviewQuestion::class.java),
            xPos = 350f,
            yPos = 550f
        ),
        ConceptEntity(
            id = "epipolar",
            title = "Epipolar Geometry & Essential Matrix",
            category = "Geometry",
            shortDesc = "The projective geometry of multi-view stereo vision. How two-camera setups constraint corresponding pixel matches on epipolar lines.",
            difficulty = "Advanced",
            intuitiveExplanationBeginner = """
                Imagine taking a photo of a house from the left side, and then another photo from the right. If you choose a point—say, the chimney tip—in the first photo, where should you look for it in the second photo?
                
                Without geometry, you would have to search the entire second image, pixel-by-pixel! But thanks to Epipolar Geometry, we know that the chimney tip MUST lie along a single straight line in the second photo (the epipolar line). It is like having a laser guide constraint. This mathematical reduction simplifies 2D search into 1D, making 3D scanning, depth cameras, and stereo vision (like our two eyes) incredibly fast and accurate!
            """.trimIndent(),
            intuitiveExplanationIntermediate = """
                Epipolar Geometry describes the projective geometry between two pinhole camera views. For a 3D point X projected onto x in View 1 and x' in View 2, the camera centers C and C' form an 'epipolar plane' with X. 
                
                The intersection of this plane with the image planes creates 'epipolar lines'. The projection of camera C' onto View 1 is the 'epipole' e, and all epipolar lines radiate from it. This geometric constraint is algebraically captured by the Essential Matrix E (for calibrated cameras) or the Fundamental Matrix F (uncalibrated). The fundamental constraint states that x'^T * E * x = 0.
            """.trimIndent(),
            intuitiveExplanationAdvanced = """
                Formally, let x and x' be normalized homogeneous image coordinates (K^-1 * pixel_coord). The 3D point X relates to the cameras via a rotation R and translation t.
                
                The projection ray vector x' must be coplanar with the baseline vector t and the rotated projection ray vector R x. The coplanarity of these three vectors in the second camera's frame is expressed by their triple scalar product:
                x'^T · (t × (R x)) = 0
                
                Representing cross product as a skew-symmetric matrix [t]_×:
                x'^T · [t]_× · R · x = 0
                
                Defining Essential Matrix E = [t]_× · R, we get the Epipolar Constraint: x'^T · E · x = 0. E has 5 degrees of freedom and is solved using SVD via the 8-point algorithm, subject to the internal constraint that its singular values are (σ, σ, 0).
            """.trimIndent(),
            formalMath = """
                The Epipolar Constraint is:
                
                x'^T E x = 0  (Calibrated)
                p'^T F p = 0  (Uncalibrated, pixel coordinates p)
                
                Where:
                - E = [t]_× R  (Essential Matrix, [t]_× is the skew-symmetric cross-product matrix)
                - F = K'^(-T) E K^(-1) (Fundamental Matrix, K is camera calibration matrix)
                
                The Epipolar Line l' in the second image for a point x in the first image is:
                l' = E x  (such that x'^T l' = 0)
                
                Skew-symmetric translation matrix [t]_× for t = [t_x, t_y, t_z]^T:
                [[  0, -t_z,  t_y],
                 [ t_z,   0, -t_x],
                 [-t_y,  t_x,   0]]
            """.trimIndent(),
            proofSketch = """
                Theorem: The epipolar constraint x'^T E x = 0 holds for corresponding projections in two calibrated views.
                
                Proof Sketch:
                1. Let P be a 3D point. In Camera 1 frame, its coordinates are X. In Camera 2 frame, its coordinates are X' = R(X - t).
                2. Projective rays are parallel to homogeneous coordinates: x ≈ X and x' ≈ X'.
                3. The vectors X', t, and R X are coplanar. This is because X' is a linear combination of baseline translation t and rotated vector R X.
                4. Therefore, their scalar triple product is zero:
                   X'^T · (t × R X) = 0
                5. Since projection rays x and x' are collinear with 3D points X and X':
                   x'^T · (t × (R x)) = 0
                6. Substitute the skew-symmetric cross-product matrix [t]_× for the cross-product operation:
                   x'^T · ([t]_× R) · x = 0
                7. Defining E = [t]_× R, we obtain x'^T E x = 0. Q.E.D.
            """.trimIndent(),
            realWorldApps = """
                - **Stereo Depth Reconstruction**: Restricting matching pixel searches to epipolar lines to compute disparity and depth.
                - **Structure from Motion (SfM)**: Reconstructing 3D cities or objects from sequences of photos.
                - **Visual Odometry & SLAM**: Tracking robot camera trajectory by solving relative pose from matched features.
                - **Multi-View 3D Tracking**: Calibrating cameras and tracking coordinates across large physical spaces.
            """.trimIndent(),
            pythonCode = """
                import cv2
                import numpy as np
                
                # Recover relative camera motion between two views
                def recover_camera_motion(pts1, pts2, K):
                    # pts1, pts2 are N x 2 coordinates of matched features
                    # K is the 3x3 camera calibration matrix
                    
                    # Compute Essential Matrix using RANSAC
                    E, mask = cv2.findEssentialMat(
                        points1=pts1,
                        points2=pts2,
                        cameraMatrix=K,
                        method=cv2.RANSAC,
                        prob=0.999,
                        threshold=1.0
                    )
                    
                    # Recover relative rotation R and translation t
                    _, R, t, recover_mask = cv2.recoverPose(E, pts1, pts2, K, mask=mask)
                    
                    return R, t, E
            """.trimIndent(),
            diagramMermaid = """
                graph TD
                    P[3D Point X] -->|Projects to| x1[Point x in Cam 1]
                    P -->|Projects to| x2[Point x' in Cam 2]
                    C1[Camera Center C] --- C2[Camera Center C']
                    C1 & P & C2 -->|Forms| Plane[Epipolar Plane]
                    Plane -->|Intersects Cam 1| l1[Epipolar Line l]
                    Plane -->|Intersects Cam 2| l2[Epipolar Line l']
            """.trimIndent(),
            researchPapersJson = toJsonListString(listOf(
                ResearchPaper(
                    title = "A Computer Algorithm for Reconstructing a Scene from Two Projections",
                    authors = "H. Christopher Longuet-Higgins",
                    year = "1981",
                    importance = "Introduced the Essential matrix and the famous 8-point algorithm, providing the algebraic foundation for modern 3D reconstruction and stereo vision.",
                    url = "https://www.nature.com/articles/293133a0"
                ),
                ResearchPaper(
                    title = "In Defense of the Eight-Point Algorithm",
                    authors = "Richard I. Hartley",
                    year = "1997",
                    importance = "Demonstrated that normalizing pixel coordinates makes the 8-point algorithm highly robust and stable under real-world noise, transforming it into a practical SLAM utility.",
                    url = "https://ieeexplore.ieee.org/document/564102"
                )
            ), ResearchPaper::class.java),
            quizJson = toJsonListString(listOf(
                QuizQuestion(
                    id = 1,
                    question = "What is the difference between the Essential Matrix (E) and the Fundamental Matrix (F)?",
                    options = listOf(
                        "E is used for uncalibrated cameras, F is for calibrated cameras.",
                        "E is defined in normalized camera coordinates, whereas F is defined in pixel coordinates and includes camera calibration K.",
                        "E has a rank of 3, while F has a rank of 2.",
                        "E only models rotation, while F only models translation."
                    ),
                    correctOptionIndex = 1,
                    explanation = "E operates on normalized coordinates (calibrated cameras), whereas F operates directly on raw image pixels by incorporating the intrinsic camera matrices K and K' via F = K'^(-T) E K^-1."
                ),
                QuizQuestion(
                    id = 2,
                    question = "What is the mathematical rank of a valid Fundamental Matrix F?",
                    options = listOf(
                        "Rank 3",
                        "Rank 2",
                        "Rank 1",
                        "It depends on the number of matched features."
                    ),
                    correctOptionIndex = 1,
                    explanation = "A valid Fundamental Matrix F must have a rank of exactly 2 because the translation skew-symmetric matrix [t]_× has a null space along the direction of translation, making F singular (det(F) = 0)."
                )
            ), QuizQuestion::class.java),
            interviewQuestionsJson = toJsonListString(listOf(
                InterviewQuestion(
                    question = "How does SVD enforce the internal constraint on the Essential Matrix during estimation?",
                    answer = "A raw estimate of E solved via linear equations often violates its internal constraints due to noise. A mathematically valid Essential Matrix must have singular values of (σ, σ, 0). To enforce this, we decompose the estimated E via SVD: E = U * diag(r, s, t) * V^T. We then replace the singular values with ( (r+s)/2, (r+s)/2, 0 ) and reconstruct E_projected = U * diag(σ, σ, 0) * V^T. This is the closest valid Essential matrix under the Frobenius norm."
                ),
                InterviewQuestion(
                    question = "What are the four potential camera pose solutions returned when decomposing the Essential Matrix E, and how do we resolve the ambiguity?",
                    answer = "Decomposing E = [t]_× R via SVD yields 4 mathematical pairings: (R_1, t), (R_1, -t), (R_2, t), and (R_2, -t). To find the single physically correct solution, we perform a 'triangulation check' or 'cheirality check' on a matched point. The correct solution is the only one where the triangulated 3D point lies in front of BOTH cameras (positive depth Z > 0)."
                )
            ), InterviewQuestion::class.java),
            xPos = 150f,
            yPos = 180f
        )
    )

    val relationships = listOf(
        ConceptRelationshipEntity(fromId = "svd", toId = "epipolar", relationType = "PREREQUISITE"),
        ConceptRelationshipEntity(fromId = "sobel", toId = "fourier", relationType = "PREREQUISITE"),
        ConceptRelationshipEntity(fromId = "sobel", toId = "cnn", relationType = "PREREQUISITE"),
        ConceptRelationshipEntity(fromId = "backprop", toId = "cnn", relationType = "PREREQUISITE")
    )
}
